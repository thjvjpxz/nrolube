<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="shortcut icon" href="{{ asset('images/logo.png') }}" />
  <title>@yield('title')</title>
  <meta name="keywords" content="nrolube, ngọc rồng lậu, game ngọc rồng lậu, nro lậu, nr lậu">
  <meta name="description"
    content="Website chính thức của Ngọc rồng Lube – Game Bay Vien Ngoc Rong Mobile nhập vai trực tuyến trên máy tính và điện thoại về Game 7 Viên Ngọc Rồng hấp dẫn nhất hiện nay!" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
    integrit6y="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&display=swap" rel="stylesheet" />
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" href="{{ asset('css/style.css') }}" />
  <script src="https://www.google.com/recaptcha/api.js" async defer></script>
</head>

<body style="background-color: #b3afaf;">
  <div class="container-fluid">
    <div class="row justify-content-center ">
      <div class="width-max col-md">
        <img src="{{ asset('images/12.png') }}" alt="" height="12" />
        <span style="line-height: 10px; font-size: 10px">
          Dành cho người chơi trên 12 tuổi. Chơi quá 180 phút mỗi ngày sẽ hại
          sức khỏe.
        </span>
        <div class="bg-secondary-color rounded-1 rounded-bottom-0 pb-1">
          <div class="text-center py-3 text-white fs-1 fw-bold" style="font-family: 'Dancing Script', cursive">
            NGỌC RỒNG LUBE
          </div>
          <div class="col-10 mx-auto d-flex gap-2 mb-3 flex-wrap justify-content-center">
            <a href="/" class="btn-index py-1 px-4 fs-6 btn btn-primary-color btn-nav text-white">Trang chủ</a>
            <a href="{{route('account.register')}}" class="btn-register py-1 px-4 fs-6 btn btn-primary-color btn-nav text-white">Đăng ký</a>
            <a href="/download" class="btn-download py-1 px-4 fs-6 btn btn-primary-color btn-nav text-white">...</a>
            <a href="https://zalo.me/g/ukrchb490"
              class="btn-box py-1 px-4 fs-6 btn btn-primary-color btn-nav text-white">Box Zalo</a>
          </div>
          <div class="mx-3 pb-3">
            <img src="{{ asset('images/banner.png') }}" alt="banner" class="img-fluid" />
          </div>
          <div class="row pb-3">
            <div class="col-md-10 mx-auto d-flex gap-2 flex-wrap justify-content-center ">
              <a href="https://www.mediafire.com/file/zv3suiklpaq2wz9/thi123.apk/file"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Tải APK manhhdc</a>
              <a href="https://www.mediafire.com/file/sct6cpnpsyyqw2k/thi123_koi.apk/file"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Tải APK koi</a>
              <a href="https://www.mediafire.com/file/5glbkn9qyn8kwag/MOD_KOI_230.rar/file"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Tải PC koi</a>
              <a href="https://www.mediafire.com/file/w4gy8n1wlnwcrfu/Koi.ipa/file"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Tải IOS</a>
              <a href="https://www.mediafire.com/file/l4x47qhp1yf0kzu/thijar.jar/file"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Tải JAR</a>
              <a href="https://drive.google.com/drive/folders/1HYyicCctaQSQp9vGDnGR6I4P0ZxH6dnX?usp=sharing"
                class="btn btn-primary-color text-white btn-nav px-3 py-1">Hướng dẫn tải game cho IOS</a>
            </div>
          </div>
        </div>
        <!-- Body -->
        <div class="bg-primary-color content">
          <div class="px-2">
            <h3 class="text-primary-color pt-3 mb-0 fw-bold text-center">@yield('title-content')</h3>
            <div class="py-3">
              @yield('content')
            </div>
          </div>
        </div>
        <div>
          <p style="font-size: 12px" class="text-white bg-secondary-color rounded-1 rounded-top-0 text-center py-3">
            Trò chơi không có bản quyền chính thức, hãy cân nhắc kỹ trước khi
            tham gia.
          </p>
        </div>
      </div>
    </div>
  </div>
  @if (session('message') && session('type'))
    <div class="toast-container rounded position-fixed bottom-0 end-0 p-3">
      <div id="liveToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-body bg-{{ session('type') }} d-flex align-items-center justify-content-between">
          <div class="d-flex justify-content-center align-items-center gap-2">
            @if (session('type') == 'success')
              <i class="fas fa-check-circle text-light fs-5"></i>
            @elseif(session('type') == 'danger' || session('type') == 'warning')
              <i class="fas fa-xmark-circle text-light fs-5"></i>
            @elseif(session('type') == 'info' || session('type') == 'secondary')
              <i class="fas fa-info-circle text-light fs-5"></i>
            @endif
            <h6 class="h6 text-white m-0">{{ session('message') }}</h6>
          </div>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
      </div>
    </div>
  @endif
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"
    integrity="sha512-v2CJ7UaYy4JwqLDIrZUI/4hqeoQieOmAZNXBeQyjo21dadnwR+8ZaIJVT8EE2iyI61OV8e6M8PP2/4hpQINQ/g=="
    crossorigin="anonymous" referrerpolicy="no-referrer"></script>
  <script src="{{ asset('js/app.js') }}"></script>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
    integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</body>

</html>
