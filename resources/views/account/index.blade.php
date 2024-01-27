@extends('layouts.app')
@section('title', 'Trang chủ - Đăng nhập')
@section('content')
@section('title-content', 'Chào mừng ' . $account->username . ' đến với Ngọc Rồng Lube')
<div class="col-10 mx-auto">
  <div class="card border-primary-color border-2">
    <div class="card-header bg-secondary-color text-white fs-5 fw-bold">Thông tin tài khoản</div>
    <div class="card-body">
      <div class="row mx-3 mx-md-5">
        <div
          class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1 border-end-0">
          Tài khoản
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">{{ $account->username }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div
          class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1 border-end-0">
          Email
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ $account->gmail ?? 'Chưa có email' }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div
          class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1 border-end-0">
          Trạng thái
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ $account->active ? 'Đã kích hoạt' : 'Chưa kích hoạt' }}</div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div
          class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1 border-end-0">
          Số dư
        </div>
        <div class="col-7 py-1 px-2 border-2 border border-primary-color rounded-end-1">
          {{ number_format($account->vnd, 0, ',', '.') }} VNĐ
        </div>
      </div>
      <div class="row mx-3 mx-md-5 mt-3">
        <div
          class="col-5 py-1 px-2 border-2 border border-primary-color bg-secondary-color text-white rounded-start-1 border-end-0">
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
          <button type="button" data-bs-toggle="modal" data-bs-target="#i{{ $account->id }}"
            class="btn btn-secondary-color rounded-1">Kích hoạt tài khoản</button>
          {{-- Modal --}}
          <div class="modal fade" id="i{{ $account->id }}" tabindex="-1">
            <div class="modal-dialog modal-dialog-centered">
              <div class="modal-content">
                <div class="modal-header bg-secondary-color text-white">
                  <h5 class="modal-title">Xác nhận kích hoạt</h5>
                  <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                  Bạn có chắc chắn muốn kích hoạt tài khoản <span
                    class="text-danger fw-bold">{{ $account->username }}</span>? (Bạn cần có 10.000 VNĐ trong tài khoản)
                </div>
                <div class="modal-footer border-primary-color">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                  <button type="button" id="btnActive" data-id="{{ $account->id }}"
                    class="btn btn-secondary-color">Kích hoạt</button>
                </div>
              </div>
            </div>
          </div>
          {{-- End Modal --}}
        @endif
        <a href="{{ route('naptien') }}" class="btn btn-secondary-color rounded-1">Nạp tiền</a>
        @if ($account->gmail == null)
          <button type="button" data-bs-toggle="modal" data-bs-target="#addEmail{{ $account->id }}"
            class="btn btn-secondary-color rounded-1">Thêm email</button>
          {{-- Modal --}}
          <div class="modal fade" id="addEmail{{ $account->id }}" tabindex="-1">
            <div class="modal-dialog modal-dialog-centered">
              <div class="modal-content">
                <div class="modal-header bg-secondary-color text-white">
                  <h5 class="modal-title">Thêm email mới</h5>
                  <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                  <div class="input-group">
                    <span class="input-group-text px-4 bg-secondary-color border-primary-color">@</span>
                    <div class="form-floating">
                      <input type="email" class="form-control border-primary-color" id="inputEmailAdd"
                        placeholder="Email">
                      <label for="inputEmailAdd">Địa chỉ email</label>
                    </div>
                  </div>
                </div>
                <div class="modal-footer border-primary-color">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                  <button type="button" id="btnAddEmail" data-id="{{ $account->id }}"
                    class="btn btn-secondary-color">Thêm</button>
                </div>
              </div>
            </div>
          </div>
          {{-- End Modal --}}
        @endif
        @if ($account->is_admin == 1)
          <button type="button" data-id="{{ $account->id }}" id="btnListItem"
            class="btn btn-secondary-color rounded-1">Danh sách item</button>

          {{-- Nap tien --}}
          <button type="button" data-bs-toggle="modal" data-bs-target="#i{{ $account->id }}"
            class="btn btn-secondary-color rounded-1">Cộng tiền</button>
          {{-- Modal --}}
          <div class="modal fade" id="i{{ $account->id }}" tabindex="-1">
            <div class="modal-dialog modal-dialog-centered">
              <div class="modal-content">
                <div class="modal-header bg-secondary-color text-white">
                  <h5 class="modal-title">Cộng tiền</h5>
                  <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                  <div class="input-group">
                    <span class="input-group-text px-4 bg-secondary-color border-primary-color">Tài khoản</span>
                    <div class="form-floating">
                      <input type="text" class="form-control border-primary-color" id="inputTaiKhoan"
                        placeholder="Tài khoản">
                      <label for="inputTaiKhoan">Tài khoản</label>
                    </div>
                  </div>
                  <div class="input-group mt-3">
                    <span class="input-group-text px-4 bg-secondary-color border-primary-color">Số tiền</span>
                    <div class="form-floating">
                      <input type="text" class="form-control border-primary-color" id="inputSoTien"
                        placeholder="Số tiền">
                      <label for="inputSoTien">Số tiền</label>
                    </div>
                  </div>
                </div>
                <div class="modal-footer border-primary-color">
                  <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                  <button type="button" id="btnCongTien"
                    class="btn btn-secondary-color">Cộng</button>
                </div>
              </div>
            </div>
          </div>
          {{-- End Modal --}}
        @endif
        <a href="{{ route('account.logout') }}" class="btn btn-secondary rounded-1">Đăng xuất</a>
      </div>
    </div>
  </div>
</div>
@endsection
@section('script')
<script type="text/javascript">
  $(document).ready(function() {
    function ajaxAccount(btn, url, data) {
      $(btn).on('click', function() {
        $.ajax({
          url: url,
          type: "POST",
          data: {
            _token: $("meta[name='csrf-token']").attr("content"),
            data: data
          },
          success: function(data) {
            window.location.href = data.url;
            // console.log(data);
          },
          error: function(xhr) {
            console.log(xhr.responseText);
          }
        });
      });
    }
    ajaxAccount('#btnActive', "/account/activeAccount", $("#btnActive").data('id'));
    ajaxAccount('#btnListItem', "/account/listItem", $("#btnListItem").data('id'));

    $("#btnAddEmail").on('click', function() {
      data = {
        id: $("#btnAddEmail").data('id'),
        email: $("#inputEmailAdd").val()
      };
      $.ajax({
        url: "/account/addEmail",
        type: "post",
        data: {
          _token: $("meta[name='csrf-token']").attr("content"),
          data: data
        },
        success: function(data) {
          window.location.href = data.url;
        },
        error: function(xhr) {
          console.log(xhr.responseText);
        }
      });
    });
    $("#btnCongTien").on('click', function() {
      data = {
        TaiKhoan: $("#inputTaiKhoan").val(),
        SoTien: $("#inputSoTien").val()
      };
      $.ajax({
        url: "/account/congTien",
        type: "post",
        data: {
          _token: $("meta[name='csrf-token']").attr("content"),
          data: data
        },
        success: function(data) {
          window.location.href = data.url;
        },
        error: function(xhr) {
          console.log(xhr.responseText);
        }
      });
    });
  });
</script>
@endsection
