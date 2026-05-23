package npc.npc_manifest;


import consts.ConstNpc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.NpcService;
import services.Service;
import services.func.TopService;
import shop.ShopService;
import sosumenh.SoSuMenhManager;
import sosumenh.SoSuMenhService;
import sosumenh.SoSuMenhTaskMain;

public class SoSuMenh extends Npc {

    public SoSuMenh(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Xem phần thưởng",//0
                    "Nhận thưởng",//1
                    "Mở khóa sổ",//2
                    "Mua lever",//3
                    "Xem xếp hạng",//4
                    "Xem thông tin hiện tại",
                    "Hòm Thư"
            ));

            String[] menus = menu.toArray(new String[0]);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "SỔ VŨ TRỤ NGỌC RỒNG ĐẠI CHIẾN MÙA 1\n Kết thúc vào 21h 30/4\nChào mừng đến với bình nguyên vô vọng", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
//                    "Xem phần thưởng",
//                    "Nhận thưởng",
//                    "Mở khóa sổ",
//                    "Nhận hủy nhiệm vụ",
//                    "Mua lever",
//                    "Xem xếp hạng"
//                    "Xem thông tin hiện tại"
                    case 0 -> {
                        SoSuMenhService.getInstance().openPanel(player);
                        break;
                    }
                    case 1 -> {
                        SoSuMenhService.getInstance().openPanel(player);
                        break;
                    }
                    case 2 -> {
                        if (player.getSession() != null && player.getSession().cash < 100_000) {
                            Service.gI().sendThongBao(player, "Bạn không đủ 100k VND");
                            return;
                        }
                        if (player.sosumenhplayer.isVip()) {
                            Service.gI().sendThongBao(player, "Bạn đã mở vip sổ rồi không thể mở tiếp!");
                            return;
                        }

                        if (PlayerDAO.subcash(player, 100_000)) {
                            player.sosumenhplayer.setVip(true);
                            Service.gI().sendThongBao(player, "Chúc mừng bạn đã mở vip sổ thành công!!");
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }
                        break;
                    }
                    case 3 -> {
                        if (player.getSession() != null && player.getSession().cash < 5_000) {
                            Service.gI().sendThongBao(player, "Bạn không đủ 5k VND");
                            return;
                        }
                        if (player.sosumenhplayer.getLevel() >= 20) {
                            Service.gI().sendThongBao(player, "Đã đạt cấp độ tối đa không thể nâng cấp");
                            return;
                        }

                        if (PlayerDAO.subcash(player, 5_000)) {
                            player.sosumenhplayer.addlevel(1);
                            Service.gI().sendThongBao(player, "Chúc mừng bạn đã nâng cấp sổ thành công cấp hiện tại:" + player.sosumenhplayer.getLevel());
                        } else {
                            Service.gI().sendThongBao(player, "Đã có lỗi xảy ra !!");
                        }
                        break;
                    }
                    case 4 -> {
                        TopService.showListTop(player, 7);
                        break;
                    }
                    case 5 -> {
                        List<Integer> id = new ArrayList<>();
                        List<Integer> idNotDone = new ArrayList<>();
                        for (SoSuMenhTaskMain taskmain : player.sosumenhplayer.ssmTaskMain) {
                            if (taskmain.finish) {
                                id.add(taskmain.idTask);
                            } else {
                                idNotDone.add(taskmain.idTask);
                            }
                        }
                        String name = "";
                        for (int i : id) {
                            name += SoSuMenhManager.getInstance().findById(i).getTask() + "\n";
                        }
                        String name1 = "";
                        for (int i : idNotDone) {
                            name1 += SoSuMenhManager.getInstance().findById(i).getTask() + "\n";
                        }
                        String vip
                                = player.sosumenhplayer.isVip() ? "\b|5|(Đã mở sổ)" : "\b|3|(Chưa mở sổ)";
                        Service.gI().sendThongBaoFromAdmin(player, "\b|5|Cấp độ Sổ ĐẠI CHIẾN  hiện tại:" + player.sosumenhplayer.getLevel() + vip
                                + "\n" + "\b|5|Nhiệm vụ phụ bạn làm tại nhiệm vụ hàng ngày ở bò mộng nhé chỉ được 3 nhiệm vụ số lượt còn lại: " + player.sosumenhplayer.getCoutday()
                                + "\n\b|3|" + "Nhiệm vụ chính đã làm xong:" + name
                                + "\n" + "Nhiệm vụ chính chưa làm xong:" + name1
                        );
                        break;
                    }
                    case 6 ->{
                        this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                "|0|Tình yêu như một dây đàn\n"
                                + "Tình vừa được thì đàn đứt dây\n"
                                + "Đứt dây này anh thay dây khác\n"
                                + "Mất em rồi anh biết thay ai?",
                                "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                + " món)",
                                "Xóa Hết\nHòm Thư", "Đóng");
                        break;
                    }
                }
            }else if (player.iDMark.getIndexMenu() == ConstNpc.MAIL_BOX) {
                switch (select) {
                    case 0:
//                                player.inventory.itemsMailBox.clear();
//                                player.inventory.itemsMailBox.addAll(GodGK.getMailBox(player));
                        ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                        break;
                    case 1:
                        NpcService.gI().createMenuConMeo(player,
                                ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                "Đồng ý", "Hủy bỏ");
                        break;
                    case 2:
                        break;
                }
            }
        }
    }
}
