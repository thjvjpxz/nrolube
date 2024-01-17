@extends('layouts.app')
@section('title', 'Đăng ký')
@section('content')
@section('title-content', 'Đăng ký')

<div class="col-10 mx-auto">
  <form action="{{ route('account.register.store') }}" method="post">
    @csrf
    <div class="input-group mb-3">
      <label for="username" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-user"></i></label>
      <input type="text" class="form-control ps-2{{ $errors->has('username') ? ' is-invalid' : '' }}" id="username"
        placeholder="Tài khoản" name="username" value="{{ old('username') }}">
      @if ($errors->has('username'))
        <div class="invalid-feedback ps-5">
          {{ $errors->first('username') }}
        </div>
      @endif
    </div>
    <div class="input-group mb-3">
      <label for="email" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-envelope"></i></label>
      <input type="email" class="form-control ps-2{{ $errors->has('gmail') ? ' is-invalid' : '' }}"
        placeholder="Địa chỉ email" id="email" name="gmail" autocomplete="email" value="{{ old('gmail') }}">
      @if ($errors->has('gmail'))
        <div class="invalid-feedback ps-5">
          {{ $errors->first('gmail') }}
        </div>
      @endif
    </div>
    <div class="input-group mb-3">
      <label for="password" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-lock"></i></label>
      <input type="password" class="form-control ps-2{{ $errors->has('password') ? ' is-invalid' : '' }}"
        placeholder="Mật khẩu" id="password" name="password">
      @if ($errors->has('password'))
        <div class="invalid-feedback ps-5">
          {{ $errors->first('password') }}
        </div>
      @endif
    </div>
    <div class="input-group">
      <label for="rePassword" class="input-group-text p-1 px-2 fs-5 border-end-0"><i class="fas fa-key"></i></label>
      <input type="password" class="form-control ps-2{{ $errors->has('rePassword') ? ' is-invalid' : '' }}"
        placeholder="Nhập lại mật khẩu" id="rePassword" name="rePassword">
      @if ($errors->has('rePassword'))
        <div class="invalid-feedback ps-5">
          {{ $errors->first('rePassword') }}
        </div>
      @endif
    </div>
    <div class="mt-3 d-flex justify-content-center">
      <div class="g-recaptcha" data-sitekey="6LdgDUIpAAAAAOJja_wDYX5OzfmUUEwIzcvR5dZy"></div>
    </div>
    <div class="d-flex flex-column justify-content-center w-100">
      <div class="d-flex justify-content-center mt-3">
        <button type="submit" class="btn btn-hover btn-secondary-color text-white px-3 py-1">
          Đăng Ký
        </button>
      </div>
      <div class="d-flex justify-content-center mt-3 gap-2">
        <span>Bạn đã có tài khoản?</span>
        <a href="{{ route('account.login') }}"
          class="text-decoration-none fw-bold text-primary-color btn-hover-underline">Đăng
          nhập ngay</a>
      </div>
    </div>
  </form>
</div>

@endsection
