$(document).ready(function () {
  const toastLiveExample = $('#liveToast');
  if (toastLiveExample.length > 0 ) {
  const toastBootstrap = new bootstrap.Toast(toastLiveExample.get(0));
  toastBootstrap.show();
  }

  if (top.location != self.location) {
    top.location = self.location
  }

  // Kiểm tra xem có tham số "success" trong URL không
  const urlParams = new URLSearchParams(window.location.search);
  const successMessage = urlParams.get('success');
  const errorMessage = urlParams.get('error');

  if (successMessage) {
    // Hiển thị thông báo thành công bằng JavaScript
    $("#modal-content-success").html(successMessage);
    $("#btnSuccess").trigger("click");
  }
  else if (errorMessage) {
    // Hiển thị thông báo lỗi bằng JavaScript
    $("#modal-content-error").html(errorMessage);
    $("#btnError").trigger("click");
  }


  var currentUriPath = window.location.pathname;
  if (currentUriPath === "/account/register") {
    $(".btn-register").addClass("active-btn");
    $(".btn-index").removeClass("active-btn");
    $(".btn-download").removeClass("active-btn");
    $(".btn-box").removeClass("active-btn");
  } else if (currentUriPath === "/index" || currentUriPath === "/") {
    $(".btn-index").addClass("active-btn");
    $(".btn-download").removeClass("active-btn");
    $(".btn-register").removeClass("active-btn");
    $(".btn-box").removeClass("active-btn");
  } else if (currentUriPath === "/download") {
    $(".btn-download").addClass("active-btn");
    $(".btn-index").removeClass("active-btn");
    $(".btn-register").removeClass("active-btn");
  }
  // document.addEventListener("contextmenu",
  //   event => event.preventDefault()
  // );
  // document.addEventListener("keydown", function (event) {
  //   if (event.ctrlKey) {
  //     event.preventDefault();
  //   }
  //   if (event.keyCode == 123) {
  //     event.preventDefault();
  //   }
  // });

});