<?php

use App\Http\Controllers\AccountController;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MailController;
/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "web" middleware group. Make something great!
|
*/

Route::get('/', function () {
    return view('index');
});
Route::get('huongdan', function () {
    return view('huongdan');
})->name('huongdan');
Route::get('naptien', function() {
    return view('baotri');
})->name('naptien');

Route::group(['prefix' => 'account', 'as' => 'account.'], function () {
    Route::get('login', [AccountController::class, 'login'])->name('login');
    Route::post('login', [AccountController::class, 'loginStore'])->name('login.store');
    Route::get('register', [AccountController::class, 'register'])->name('register');
    Route::post('register', [AccountController::class, 'registerStore'])->name('register.store');
    Route::group(['middleware' => 'checkLogin'], function () {
        Route::get('index', [AccountController::class, 'index'])->name('index');
        Route::post('activeAccount', [AccountController::class, 'activeAccount'])->name('activeAccount');
        Route::post('listItem', [AccountController::class, 'listItem'])->name('listItem')->middleware('checkAdmin');
        Route::post('congTien', [AccountController::class, 'congTien'])->name('congTien')->middleware('checkAdmin');
        Route::post('doiTien', [AccountController::class, 'doiTien'])->name('doiTien')->middleware('checkAdmin');
        Route::get('showListItem', [AccountController::class, 'showListItem'])->name('showListItem')->middleware('checkAdmin');
        Route::get('findItem', [AccountController::class, 'findItem'])->name('findItem')->middleware('checkAdmin');
        Route::post('addEmail', [AccountController::class, 'addEmail'])->name('addEmail');
        Route::get('logout', [AccountController::class, 'logout'])->name('logout');
    });
});
