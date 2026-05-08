#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

APP_NAME="nro-server"
JAR_PATH="dist/Michelin_Boy.jar"
MAIN_CLASS="server.ServerManager"
PID_FILE=".nro-server.pid"
LOG_DIR="logs"
LOG_FILE="$LOG_DIR/server.log"
CONFIG_FILE="data/config/config.properties"
MYSQL_SEED_FILE="sql/tomahoc_db.sql"

JAVA_XMS="1G"
JAVA_XMX="1G"
JAVA_XSS="512k"
JAVA_GC_OPTS="-XX:+UseZGC"

MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_DB="tomahoc_db"
MYSQL_USER="root"
MYSQL_PASS="root"

print_header() {
  echo "======================================"
  echo " NRO Server Control (Ubuntu / Ant)"
  echo "======================================"
}

usage() {
  cat <<'EOF'
Usage:
  ./serverctl.sh run
  ./serverctl.sh start
  ./serverctl.sh stop
  ./serverctl.sh status
  ./serverctl.sh logs
  ./serverctl.sh menu

Notes:
  - run   : full pipeline + run foreground (recommended first run)
  - start : full pipeline + run background
  - menu  : minimal interactive menu

Advanced commands:
  ./serverctl.sh setup | check | build | mysql | restart
EOF
}

has_cmd() {
  command -v "$1" >/dev/null 2>&1
}

ensure_dirs() {
  mkdir -p "$LOG_DIR"
}

ensure_apt() {
  if ! has_cmd apt; then
    echo "This script supports Debian/Ubuntu only."
    exit 1
  fi
}

install_if_missing() {
  local pkg="$1"
  local bin="$2"
  if has_cmd "$bin"; then
    return
  fi
  ensure_apt
  echo "[Setup] $bin not found. Installing $pkg..."
  sudo apt update
  sudo apt install -y "$pkg"
}

setup_ubuntu() {
  ensure_apt
  echo "[Setup] Installing required packages..."
  sudo apt update
  sudo apt install -y openjdk-17-jdk ant mysql-server mysql-client tmux unzip curl wget
  sudo systemctl enable --now mysql
  echo "[Setup] Done."
}

check_requirements() {
  echo "[Check] Commands:"
  for cmd in java javac ant mysql; do
    if has_cmd "$cmd"; then
      echo "  - $cmd: OK"
    else
      echo "  - $cmd: MISSING"
    fi
  done

  if [[ -f "$JAR_PATH" ]]; then
    echo "  - $JAR_PATH: FOUND"
  else
    echo "  - $JAR_PATH: NOT FOUND"
  fi

  if has_cmd systemctl && systemctl is-active mysql >/dev/null 2>&1; then
    echo "  - mysql service: RUNNING"
  else
    echo "  - mysql service: STOPPED or unavailable"
  fi
}

ensure_java_and_ant() {
  install_if_missing "openjdk-17-jdk" "java"
  install_if_missing "ant" "ant"
}

resolve_jdk_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    echo "$JAVA_HOME"
    return 0
  fi

  local java_bin java_real
  java_bin="$(command -v java || true)"
  if [[ -z "$java_bin" ]]; then
    return 1
  fi
  java_real="$(readlink -f "$java_bin")"
  dirname "$(dirname "$java_real")"
}

resolve_active_platform() {
  local platform
  platform="$(awk -F= '$1=="platform.active"{print $2; exit}' "nbproject/project.properties" 2>/dev/null || true)"
  platform="${platform//$'\r'/}"
  if [[ -z "$platform" ]]; then
    platform="JDK_21"
  fi
  echo "$platform"
}

build_ant() {
  local jdk_home active_platform
  ensure_java_and_ant
  jdk_home="$(resolve_jdk_home || true)"
  if [[ -z "$jdk_home" ]]; then
    echo "[Build] Cannot resolve JDK home from JAVA_HOME or java binary."
    exit 1
  fi
  active_platform="$(resolve_active_platform)"

  echo "[Build] ant clean jar"
  ant -D"platforms.${active_platform}.home=${jdk_home}" clean jar
  if [[ ! -f "$JAR_PATH" ]]; then
    echo "[Build] Failed: $JAR_PATH not found."
    exit 1
  fi
  echo "[Build] Done: $JAR_PATH"
}

get_property() {
  local key="$1"
  if [[ ! -f "$CONFIG_FILE" ]]; then
    return
  fi
  awk -F= -v k="$key" '
    $1==k {
      val=$2
      gsub(/\r/, "", val)
      sub(/^[ \t]+/, "", val)
      sub(/[ \t]+$/, "", val)
      print val
      exit
    }
  ' "$CONFIG_FILE"
}

normalize_config_file() {
  if [[ -f "$CONFIG_FILE" ]]; then
    sed -i 's/\r$//' "$CONFIG_FILE"
  fi
}

set_property() {
  local key="$1"
  local value="$2"
  local escaped
  escaped="$(printf '%s' "$value" | sed 's/[&|]/\\&/g')"

  if [[ ! -f "$CONFIG_FILE" ]]; then
    mkdir -p "$(dirname "$CONFIG_FILE")"
    touch "$CONFIG_FILE"
  fi
  normalize_config_file

  if grep -q "^${key}=" "$CONFIG_FILE"; then
    sed -i "s|^${key}=.*|${key}=${escaped}|" "$CONFIG_FILE"
  else
    echo "${key}=${value}" >> "$CONFIG_FILE"
  fi
}

load_mysql_from_config() {
  local v
  normalize_config_file
  v="$(get_property "database.host")"; [[ -n "${v:-}" ]] && MYSQL_HOST="$v"
  v="$(get_property "database.port")"; [[ -n "${v:-}" ]] && MYSQL_PORT="$v"
  v="$(get_property "database.name")"; [[ -n "${v:-}" ]] && MYSQL_DB="$v"
  v="$(get_property "database.user")"; [[ -n "${v:-}" ]] && MYSQL_USER="$v"
  v="$(get_property "database.pass")"; [[ -n "${v:-}" ]] && MYSQL_PASS="$v"
}

prompt_mysql_settings() {
  local input
  load_mysql_from_config

  read -rp "MySQL host [${MYSQL_HOST}]: " input
  [[ -n "$input" ]] && MYSQL_HOST="$input"

  read -rp "MySQL port [${MYSQL_PORT}]: " input
  [[ -n "$input" ]] && MYSQL_PORT="$input"

  read -rp "MySQL database [${MYSQL_DB}]: " input
  [[ -n "$input" ]] && MYSQL_DB="$input"

  read -rp "MySQL user [${MYSQL_USER}]: " input
  [[ -n "$input" ]] && MYSQL_USER="$input"

  read -rsp "MySQL password [hidden, Enter=keep current]: " input
  echo
  [[ -n "$input" ]] && MYSQL_PASS="$input"
}

mysql_jdbc_url() {
  echo "jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DB}?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=utf8&allowPublicKeyRetrieval=true"
}

check_mysql_config_match() {
  local current_host current_port current_db current_user current_pass current_url expect_url
  current_host="$(get_property "database.host")"
  current_port="$(get_property "database.port")"
  current_db="$(get_property "database.name")"
  current_user="$(get_property "database.user")"
  current_pass="$(get_property "database.pass")"
  current_url="$(get_property "database.url")"
  expect_url="$(mysql_jdbc_url)"

  if [[ "$current_host" == "$MYSQL_HOST" && "$current_port" == "$MYSQL_PORT" && "$current_db" == "$MYSQL_DB" && "$current_user" == "$MYSQL_USER" && "$current_pass" == "$MYSQL_PASS" && "$current_url" == "$expect_url" ]]; then
    echo "[MySQL] config.properties already matches."
    return 0
  fi

  echo "[MySQL] config.properties differs from current values."
  return 1
}

write_mysql_config() {
  set_property "database.host" "$MYSQL_HOST"
  set_property "database.port" "$MYSQL_PORT"
  set_property "database.name" "$MYSQL_DB"
  set_property "database.user" "$MYSQL_USER"
  set_property "database.pass" "$MYSQL_PASS"
  set_property "database.url" "$(mysql_jdbc_url)"
  echo "[MySQL] Updated $CONFIG_FILE"
}

ensure_mysql_ready() {
  install_if_missing "mysql-server" "mysqld"
  install_if_missing "mysql-client" "mysql"
  sudo systemctl enable --now mysql
}

ensure_mysql_seeded() {
  local table_count
  if [[ ! -f "$MYSQL_SEED_FILE" ]]; then
    echo "[MySQL] Seed file not found: $MYSQL_SEED_FILE (skip import)"
    return
  fi

  table_count="$(
    MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" \
      -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DB}';" 2>/dev/null || true
  )"
  table_count="${table_count//$'\r'/}"
  table_count="$(echo "$table_count" | tr -d '[:space:]')"

  if [[ ! "$table_count" =~ ^[0-9]+$ ]]; then
    echo "[MySQL] Cannot verify table count. Skip auto import."
    return
  fi

  if [[ "$table_count" -eq 0 ]]; then
    echo "[MySQL] First run detected (empty DB). Importing ${MYSQL_SEED_FILE}..."
    MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" "$MYSQL_DB" < "$MYSQL_SEED_FILE"
    echo "[MySQL] Import completed."
  else
    echo "[MySQL] Existing tables: $table_count (skip auto import)."
  fi
}

mysql_init() {
  local test_ok=0 fallback_user fallback_pass input
  ensure_mysql_ready
  prompt_mysql_settings

  if [[ "$MYSQL_USER" == *"'"* || "$MYSQL_PASS" == *"'"* || "$MYSQL_DB" == *"'"* ]]; then
    echo "[MySQL] user/pass/db cannot contain single quote (')."
    exit 1
  fi

  echo "[MySQL] Creating database/user if missing..."
  sudo mysql <<EOF
CREATE DATABASE IF NOT EXISTS \`${MYSQL_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_PASS}';
GRANT ALL PRIVILEGES ON \`${MYSQL_DB}\`.* TO '${MYSQL_USER}'@'localhost';
FLUSH PRIVILEGES;
EOF

  if ! check_mysql_config_match; then
    write_mysql_config
  fi

  echo "[MySQL] Testing connection..."
  if MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -e "SELECT 1;" >/dev/null 2>&1; then
    test_ok=1
  fi

  if [[ "$test_ok" -eq 0 && "$MYSQL_USER" == "root" ]]; then
    echo "[MySQL] root login failed (Ubuntu often uses auth_socket for root)."
    echo "[MySQL] Switching to an application user is recommended."
    fallback_user="nro"
    fallback_pass=""
    read -rp "App MySQL user [${fallback_user}]: " input
    [[ -n "$input" ]] && fallback_user="$input"
    while [[ -z "$fallback_pass" ]]; do
      read -rsp "App MySQL password (required): " fallback_pass
      echo
      if [[ -z "$fallback_pass" ]]; then
        echo "Password cannot be empty."
      fi
    done

    MYSQL_USER="$fallback_user"
    MYSQL_PASS="$fallback_pass"
    echo "[MySQL] Creating app user '${MYSQL_USER}'..."
    sudo mysql <<EOF
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_PASS}';
GRANT ALL PRIVILEGES ON \`${MYSQL_DB}\`.* TO '${MYSQL_USER}'@'localhost';
FLUSH PRIVILEGES;
EOF
    write_mysql_config

    if MYSQL_PWD="$MYSQL_PASS" mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -e "SELECT 1;" >/dev/null 2>&1; then
      test_ok=1
    fi
  fi

  if [[ "$test_ok" -eq 0 ]]; then
    echo "[MySQL] Connection test failed for user '${MYSQL_USER}'."
    echo "Check host/port/user/pass and run: ./serverctl.sh mysql"
    exit 1
  fi

  ensure_mysql_seeded
  echo "[MySQL] Ready."
}

parse_ram_to_mib() {
  local raw="${1// /}"
  raw="${raw,,}"
  if [[ "$raw" =~ ^([0-9]+)(m|mb|g|gb)?$ ]]; then
    local amount="${BASH_REMATCH[1]}"
    local unit="${BASH_REMATCH[2]:-m}"
    if [[ "$unit" == "g" || "$unit" == "gb" ]]; then
      echo $((amount * 1024))
    else
      echo "$amount"
    fi
    return
  fi
  return 1
}

choose_java_memory() {
  local mode total_mib xmx_mib xms_mib input ram_mib
  read -rp "RAM mode [auto/manual] (default auto): " mode
  mode="${mode,,}"

  if [[ "$mode" == "manual" || "$mode" == "m" ]]; then
    while true; do
      read -rp "Enter RAM to use (e.g. 2G, 2048M): " input
      if ram_mib="$(parse_ram_to_mib "$input")"; then
        break
      fi
      echo "Invalid RAM format. Try 2G or 2048M."
    done
    xmx_mib=$((ram_mib * 90 / 100))
    xms_mib=$((xmx_mib * 50 / 100))
    ((xmx_mib < 256)) && xmx_mib=256
    ((xms_mib < 128)) && xms_mib=128
    JAVA_XMX="${xmx_mib}m"
    JAVA_XMS="${xms_mib}m"
    echo "[Java] Manual RAM=${ram_mib}MiB => Xmx=${JAVA_XMX}, Xms=${JAVA_XMS}"
  else
    total_mib="$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo)"
    (( total_mib < 1 )) && total_mib=1024
    xmx_mib=$((total_mib * 70 / 100))
    xms_mib=$((xmx_mib * 50 / 100))
    ((xmx_mib < 512)) && xmx_mib=512
    ((xms_mib < 256)) && xms_mib=256
    JAVA_XMX="${xmx_mib}m"
    JAVA_XMS="${xms_mib}m"
    echo "[Java] Auto total=${total_mib}MiB => Xmx=${JAVA_XMX}, Xms=${JAVA_XMS}"
  fi
}

is_running() {
  if [[ -f "$PID_FILE" ]]; then
    local pid
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [[ -n "${pid:-}" ]] && kill -0 "$pid" >/dev/null 2>&1; then
      return 0
    fi
  fi
  return 1
}

run_java_foreground() {
  echo "[Run] Starting foreground..."
  java -Xms"$JAVA_XMS" -Xmx"$JAVA_XMX" -Xss"$JAVA_XSS" $JAVA_GC_OPTS -cp "$JAR_PATH:lib/*" "$MAIN_CLASS"
}

start_java_background() {
  ensure_dirs
  if is_running; then
    echo "[Start] $APP_NAME already running (PID: $(cat "$PID_FILE"))."
    return
  fi

  echo "[Start] Starting background..."
  nohup java -Xms"$JAVA_XMS" -Xmx"$JAVA_XMX" -Xss"$JAVA_XSS" $JAVA_GC_OPTS \
    -cp "$JAR_PATH:lib/*" "$MAIN_CLASS" >>"$LOG_FILE" 2>&1 &

  local pid=$!
  echo "$pid" >"$PID_FILE"
  sleep 1
  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[Start] Started. PID=$pid LOG=$LOG_FILE"
  else
    echo "[Start] Failed. Check $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
  fi
}

prepare_and_run() {
  ensure_java_and_ant
  build_ant
  mysql_init
  choose_java_memory
  run_java_foreground
}

prepare_and_start() {
  ensure_java_and_ant
  build_ant
  mysql_init
  choose_java_memory
  start_java_background
}

stop_server() {
  if ! is_running; then
    echo "[Stop] $APP_NAME is not running."
    rm -f "$PID_FILE"
    return
  fi

  local pid
  pid="$(cat "$PID_FILE")"
  echo "[Stop] Stopping PID $pid..."
  kill "$pid"

  for _ in {1..20}; do
    if kill -0 "$pid" >/dev/null 2>&1; then
      sleep 1
    else
      break
    fi
  done

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[Stop] Force kill..."
    kill -9 "$pid"
  fi

  rm -f "$PID_FILE"
  echo "[Stop] Done."
}

restart_server() {
  stop_server
  sleep 2
  prepare_and_start
}

status_server() {
  if is_running; then
    echo "[Status] RUNNING (PID: $(cat "$PID_FILE"))"
  else
    echo "[Status] STOPPED"
  fi
}

show_logs() {
  ensure_dirs
  [[ -f "$LOG_FILE" ]] || touch "$LOG_FILE"
  echo "[Logs] Following $LOG_FILE"
  tail -f "$LOG_FILE"
}

menu() {
  while true; do
    print_header
    echo "1) Run full pipeline (foreground)"
    echo "2) Start full pipeline (background)"
    echo "3) Status"
    echo "4) Logs"
    echo "5) Stop"
    echo "9) Advanced options"
    echo "0) Exit"
    echo
    read -rp "Choose option: " choice
    case "$choice" in
      1) prepare_and_run ;;
      2) prepare_and_start ;;
      3) status_server ;;
      4) show_logs ;;
      5) stop_server ;;
      9)
        echo "--- Advanced ---"
        echo "a) setup  (install packages)"
        echo "b) check  (requirements)"
        echo "c) build  (ant clean jar)"
        echo "d) mysql  (setup + sync config)"
        echo "e) restart"
        read -rp "Choose advanced option: " adv
        case "$adv" in
          a) setup_ubuntu ;;
          b) check_requirements ;;
          c) build_ant ;;
          d) mysql_init ;;
          e) restart_server ;;
          *) echo "Invalid advanced option." ;;
        esac
        ;;
      0) echo "Bye."; break ;;
      *) echo "Invalid choice." ;;
    esac
    echo
    read -rp "Press Enter to continue..."
    clear
  done
}

main() {
  case "${1:-menu}" in
    menu) menu ;;
    setup) setup_ubuntu ;;
    check) check_requirements ;;
    build) build_ant ;;
    mysql) mysql_init ;;
    run) prepare_and_run ;;
    start) prepare_and_start ;;
    stop) stop_server ;;
    restart) restart_server ;;
    status) status_server ;;
    logs) show_logs ;;
    -h|--help|help) usage ;;
    *) usage; exit 1 ;;
  esac
}

main "${1:-menu}"
