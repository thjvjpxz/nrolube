<?php

namespace App\Http\Controllers;

use App\Models\Account;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class AccountController extends Controller
{
    public function index()
    {
        $account = session('account');
        $account = Account::find($account->id);
        return view('account.index', compact('account'));
    }
    public function listItem(Request $request)
    {
        $id = $request->data;
        $account = Account::find($id);
        if ($account->is_admin != 1) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Bạn không có quyền truy cập!');
        } else {
            $url = redirect()->route('account.showListItem');
        }
        return response()->json(['url' => $url->getTargetUrl()]);
    }
    public function showListItem()
    {
        $itemTemplate = DB::table('item_template')->get();
        return view('account.listItem', compact('itemTemplate'));
    }
    public function findItem(Request $request)
    {
        $keyword = $request->keyword;
        $keywords = '%' . $keyword . '%';
        if ($keyword == "") {
            $itemTemplate = DB::table('item_template')
                ->select('id', 'NAME', 'icon_id', 'description', DB::raw("
                    CASE 
                        WHEN gender = 0 THEN 'Trái đất'
                        WHEN gender = 1 THEN 'Namec'
                        WHEN gender = 2 THEN 'Xayda'
                        ELSE 'Tất cả' 
                    END AS gender"))
                ->get();
        } else {
            $itemTemplate = DB::table('item_template')
                ->select('id', 'NAME', 'icon_id', 'description', DB::raw("
                    CASE 
                        WHEN gender = 0 THEN 'Trái đất'
                        WHEN gender = 1 THEN 'Namec'
                        WHEN gender = 2 THEN 'Xayda'
                        ELSE 'Tất cả' 
                    END AS gender"))
                ->where('NAME', 'LIKE', $keywords)
                ->orWhere('id', 'LIKE', $keywords)
                ->get();
        }
        $htmls = '';
        foreach ($itemTemplate as $item) {
            if ($item->NAME == "")
                continue;
            $htmls .=
                '
                <tr>
                    <td class="align-middle 1">' . $item->id . '</td>
                    <td class="align-middle 2">' . $item->NAME . '</td>
                    <td class="align-middle 3">' . $item->gender . '</td>
                    <td class="align-middle 4">' . $item->icon_id . '</td>
                    <td class="align-middle 5">' . $item->description . '</td>
                </tr>';
            // if ($item->gender == 0) {
            //     $htmls .= 'Trái đất';
            // } else if ($item->gender == 1) {
            //     $htmls .= 'Namec';
            // } else {
            //     $htmls .= 'Xayda';
            // }
        }
        return $htmls;
    }
    public function logout()
    {
        session()->forget('account');
        return redirect()->route('account.login')->with('type', 'success')->with('message', 'Đăng xuất thành công!');
    }
    public function register()
    {
        return view('account.register')->with('type', 'success')->with('message', 'You have successfully registered!');
    }
    public function login()
    {
        return view('account.login');
    }
    public function activeAccount(Request $request)
    {
        $account = Account::find($request->data);
        $cost = 10000;
        $moneyHave = $account->vnd;
        if (is_null($account)) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản không tồn tại!');
        } else if ($account->active == 1) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản đã được kích hoạt!');
        } else if ($moneyHave < $cost) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Bạn không đủ tiền để kích hoạt tài khoản!');
        } else {
            $account->vnd = $moneyHave - $cost;
            $account->active = 1;
            $account->save();
            $url = redirect()->back()->with('type', 'success')->with('message', 'Kích hoạt tài khoản thành công!');
        }
        return response()->json(['url' => $url->getTargetUrl()]);
    }
    public function doiTien(Request $request) {
        $id = $request->data['id'];
        $soTienDoi = $request->data['SoTienDoi'];
        $account = Account::find($id);
        if (is_null($account)) {
            return response()->json(['url' => redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản không tồn tại!')->getTargetUrl()]);
        }
        if ($account->vnd < 10000) {
            return response()->json(['url' => redirect()->back()->with('type', 'danger')->with('message', 'Phải đổi ít nhất 10000VNĐ!')->getTargetUrl()]);
        }
        if ($account->vnd < $soTienDoi) {
            return response()->json(['url' => redirect()->back()->with('type', 'danger')->with('message', 'Bạn không đủ tiền để đổi!')->getTargetUrl()]);
        }
        $account->vnd = $account->vnd - $soTienDoi;
        $account->coin += $soTienDoi;
        $account->save();
        return response()->json(['url' => redirect()->back()->with('type', 'success')->with('message', 'Đổi tiền thành công!')->getTargetUrl()]);

    }
    public function addEmail(Request $request)
    {
        $account = Account::find($request->data['id']);
        $email = $request->data['email'];
        $url = "";
        if (is_null($account)) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản không tồn tại!');
        } else if ($email == "") {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Email không được để trống!');
        } else if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $url = redirect()->back()->with('type', 'danger')->with('message', 'Email không đúng định dạng!');
        } else {
            $checkExist = Account::where('gmail', $email)->first();
            if (!is_null($checkExist)) {
                $url = redirect()->back()->with('type', 'danger')->with('message', 'Email đã tồn tại!');
            } else {
                $account->gmail = $email;
                $account->save();
                $url = redirect()->back()->with('type', 'success')->with('message', 'Thêm email thành công!');
            }
        }
        return response()->json(['url' => $url->getTargetUrl()]);
        // return response()->json(['url' => $email]);
    }
    public function loginStore(Request $request)
    {
        $recaptcha = $request->input('g-recaptcha-response');
        if (is_null($recaptcha)) {
            return redirect()->back()->with('type', 'danger')->with('message', 'Vui lòng xác nhận captcha!');
        }
        $request->validate([
            'username' => 'required',
            'password' => 'required'
        ], [
            'username.required' => 'Không được để trống tài khoản!',
            'password.required' => 'Không được để trống mật khẩu!'
        ]);
        $account = Account::where('username', $request->username)->where('password', $request->password)->first();
        if (is_null($account)) {
            return redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản hoặc mật khẩu không chính xác!');
        }
        session(['account' => $account]);
        return redirect()->route('account.index')->with('type', 'success')->with('message', 'Đăng nhập thành công!');
    }
    public function registerStore(Request $request)
    {
        $recaptcha = $request->input('g-recaptcha-response');
        if (is_null($recaptcha)) {
            return redirect()->back()->with('type', 'danger')->with('message', 'Vui lòng xác nhận captcha!');
        }
        $ip = $request->ip();
        $check = DB::table('history_register_ip')->where('ip_address', $ip)->first();
        if (!is_null($check) && $check->count_register >= 3) {
            return redirect()->back()->with('type', 'danger')->with('message', 'Bạn đã đăng ký quá số lần cho phép!');
        } else if (!is_null($check) && $check->count_register < 3) {
            $count = $check->count_register + 1;
            DB::table('history_register_ip')->where('ip_address', $ip)->update(['count_register' => $count]);
        } else {
            DB::table('history_register_ip')->insert(['ip_address' => $ip, 'count_register' => 1]);
        }
        $request->validate([
            'username' => 'required|min:3|max:255|unique:account,username',
            'gmail' => [
                'required',
                'email',
                'regex: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/',
                'unique:account,gmail'
            ],
            'password' => 'required',
            'rePassword' => 'required|same:password'
        ], [
            'username.required' => 'Không được để trống tài khoản!',
            'username.unique' => 'Tài khoản đã tồn tại!',
            'username.min' => 'Tài khoản phải có ít nhất 3 ký tự!',
            'username.max' => 'Tài khoản nhiều nhất 255 ký tự!',
            'gmail.required' => 'Không được để trống email!',
            'gmail.email' => 'Email không đúng định dạng!',
            'gmail.regex' => 'Email không đúng định dạng!',
            'gmail.unique' => 'Email đã tồn tại!',
            'password.required' => 'Không được để trống mật khẩu!',
            'rePassword.required' => 'Không được để trống nhập lại mật khẩu!',
            'rePassword.same' => 'Mật khẩu nhập lại không khớp!'
        ]);
        $account = new Account();
        $account->username = $request->username;
        $account->gmail = $request->gmail;
        $account->password = $request->password;
        $account->save();
        return redirect()->route('account.login')->with('type', 'success')->with('message', 'Đăng ký tài khoản thành công!');
    }
    public function congTien(Request $request) {
        $taikhoan = $request->data['TaiKhoan'];
        $soTien = $request->data['SoTien'];
        $account = Account::where('username', $taikhoan)->first();
        if (is_null($account)) {
            return response()->json(['url' => redirect()->back()->with('type', 'danger')->with('message', 'Tài khoản không tồn tại!')->getTargetUrl()]);
        }
        $account->vnd = $account->vnd + $soTien;
        $account->save();
        return response()->json(['url' => redirect()->back()->with('type', 'success')->with('message', 'Cộng tiền thành công!')->getTargetUrl()]);
    }
}
