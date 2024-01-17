<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Mail\IdentityAccount;
use Illuminate\Support\Facades\Mail;

class MailController extends Controller
{
    public function index() {
        $mailData = [
            'title' => 'Mail from NroLube',
            'body' => 'This is for testing email using smtp'
        ];

        Mail::to('thi12a3qv2@gmail.com')->send(new IdentityAccount($mailData));
        dd('Mail sent successfully');
    }
}
