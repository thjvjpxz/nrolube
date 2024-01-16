<?php

namespace App\Http\Controllers;

use App\Models\Account;
use Illuminate\Http\Request;

class AccountController extends Controller
{
    public function register() {
        return view('account.register')->with('type', 'success')->with('message', 'You have successfully registered!');
    }
    public function login() {
        return view('account.login');
    }
    public function registerStore(Request $request) {
        $recaptcha = $request->input('g-recaptcha-response');
        if (is_null($recaptcha)) {
            return redirect()->back()->with('type', 'danger')->with('message', 'Vui lòng xác nhận captcha!');
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
}
