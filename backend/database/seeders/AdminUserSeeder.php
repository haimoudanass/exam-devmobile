<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;

class AdminUserSeeder extends Seeder
{
    public function run(): void
    {
        User::updateOrCreate(
            ['email' => 'admin@smartcity.local'],
            [
                'name' => 'Admin Commune',
                'password' => 'admin123',
                'role' => User::ROLE_ADMIN,
            ]
        );

        User::where('email', 'demo@smartcity.local')->update([
            'role' => User::ROLE_CITIZEN,
        ]);
    }
}
