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

Route::group(['prefix' => 'account', 'as' => 'account.'], function() {
    Route::get('login', [AccountController::class, 'login'])->name('login');
    Route::get('register', [AccountController::class, 'register'])->name('register');
    Route::post('register', [AccountController::class, 'registerStore'])->name('register.store');
});

Route::get('send-mail', [MailController::class, 'index'])->name('send.mail');