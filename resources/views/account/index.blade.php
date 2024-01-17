@extends('layouts.app')
@section('title', 'Trang chủ - Đăng nhập')
@section('content')
@section('title-content', 'Chào mừng ' . $account->username . ' đến với Ngọc Rồng Lube')
<div class="col-10 mx-auto">
  <div class="card border-primary-color border-2">
    <div class="card-header bg-secondary-color text-white fs-5 fw-bold">Thông tin tài khoản</div>
    <div class="card-body">
      <div class="row mx-3 mx-md-5">
        <div class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1">
          Tài khoản
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">{{ $account->username }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1">
          Email
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ $account->gmail ?? 'Chưa có email' }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1">
          Trạng thái
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ $account->active ? 'Đã kích hoạt' : 'Chưa kích hoạt' }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1">
          Số dư
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ number_format($account->vnd, 0, ',', '.') }} VNĐ
        </div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1">
          Tổng tiền đã nạp
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ number_format($account->tongnap, 0, ',', '.') }} VNĐ
        </div>
      </div>
    </div>
    <div class="card-footer border-primary-color border-2 py-0">
      <div class="d-flex gap-1 flex-wrap justify-content-center my-3">
        @if ($account->active == 0)
          <a id="btnActive" data-id="{{ $account->id }}" class="btn btn-success rounded-1">Kích hoạt tài khoản</a>
        @endif
        <a href="{{ route('account.logout') }}" class="btn btn-success rounded-1">Nạp tiền</a>
        <a href="{{ route('account.logout') }}" class="btn btn-success rounded-1">Thêm email</a>
        <a href="{{ route('account.logout') }}" class="btn btn-secondary rounded-1">Đăng xuất</a>
      </div>
    </div>
  </div>
</div>
@endsection
@section('script')
<script type="text/javascript">
  $(document).ready(function() {
    $("#btnActive").on('click', function() {
      $.ajax({
        url: "/account/activeAccount",
        type: "POST",
        data: {
          _token: $("meta[name='csrf-token']").attr("content"),
          id: $(this).data('id')
        },
        success: function(data) {
          window.location.href = data.url;
        },
      });
    });
  });
</script>
@endsection
