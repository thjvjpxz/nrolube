@extends('layouts.app')
@section('title', 'Danh sách vật phẩm')
@section('content')
@section('title-content', 'Danh sách vật phẩm')
<div class="col-10 mx-auto ">
  <div class="input-group-sm mb-3">
    <input type="text" class="form-control" id="keyword" placeholder="Nhập từ cần tìm kiếm">
  </div>
  <table class="table table-bordered">
    <thead>
      <tr>
        <td class="align-middle">ID</td>
        <td class="align-middle">Tên vật phẩm</td>
        <td class="align-middle">Hệ</td>
        <td class="align-middle">ID icon</td>
        <td class="align-middle">Mô tả</td>
      </tr>
    </thead>
    <tbody class="tbody">
      @foreach ($itemTemplate as $item)
        @if ($item->NAME == '')
          @continue
        @else
          <tr>
            <td class="align-middle">{{ $item->id }}</td>
            <td class="align-middle">{{ $item->NAME }}</td>
            <td class="align-middle">{{ $item->gender == 0 ? 'Trái đất' : ($item->gender == 1 ? 'Namec' : 'Xayda') }}
            </td>
            <td class="align-middle">{{ $item->icon_id }}</td>
            <td class="align-middle">{{ $item->description }}</td>
          </tr>
        @endif
      @endforeach
    </tbody>
  </table>
</div>

@endsection
@section('script')
  <script type="text/javascript">
    $(document).ready(function() {
      $('#keyword').keyup(function() {
        var keyword = $(this).val();
        $.ajax({
          url: "/account/findItem",
          method: 'GET',
          data: {
            keyword: keyword
          },
          success: function(data) {
            $('tbody').html(data);
          },
          error: function(xhr) {
            console.log(xhr.responseText);
          }
        });
      });
    });
  </script>

@endsection