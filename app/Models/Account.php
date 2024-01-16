<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Account extends Model
{
    use HasFactory;
    protected $table = 'account';
    protected $primaryKey = 'id';
    public $timestamps = false;
    protected $fillable = [
        'username',
        'gmail',
        'password'
    ];
    protected $hidden = [
        'password'
    ];
}
