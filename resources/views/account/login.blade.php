@extends('layouts.app')
@section('title', 'Đăng nhập')
@section('content')
@section('title-content', 'Đăng nhập')
<div class="col-10 mx-auto">
  <form action="{{route('account.login.store')}}" method="post">
    @csrf
    <div class="input-group mb-3">
      <label for="username" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-user"></i></label>
      <input type="text" class="form-control ps-2" id="username" placeholder="Tài khoản" name="username">
    </div>
    <div class="input-group mb-3">
      <label for="password" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-lock"></i></label>
      <input type="password" class="form-control ps-2" placeholder="Mật khẩu" id="password" name="password"
        autocomplete="current-password">
    </div>
    <div class="my-3 d-flex justify-content-center">
      <div class="g-recaptcha" data-sitekey="6LdgDUIpAAAAAOJja_wDYX5OzfmUUEwIzcvR5dZy"></div>
    </div>
    <div class="d-flex flex-column justify-content-center w-100">
      <div class="d-flex justify-content-center">
        <button type="submit" class="btn btn-hover btn-secondary-color text-white px-3 py-1">
          Đăng Nhập
        </button>
      </div>
      <div class="d-flex justify-content-center mt-3 gap-2">
        <span>Bạn chưa có tài khoản?</span>
        <a href="{{ route('account.register') }}"
          class="text-decoration-none fw-bold text-primary-color btn-hover-underline">Đăng
          ký ngay</a>
      </div>
    </div>

  </form>
</div>
@endsection
