<?php

namespace Database\Seeders;

use App\Models\Report;
use App\Models\User;
use App\Services\ReportClassifier;
use Illuminate\Database\Seeder;

class ReportSeeder extends Seeder
{
    public function run(): void
    {
        $classifier = new ReportClassifier;

        $user = User::firstOrCreate(
            ['email' => 'demo@smartcity.local'],
            [
                'name' => 'Demo Citizen',
                'password' => 'password123',
                'role' => User::ROLE_CITIZEN,
            ]
        );
        $user->update(['role' => User::ROLE_CITIZEN]);

        $reports = [
            [
                'title' => 'Nid de poule — Boulevard Mohammed V',
                'description' => 'Grand trou sur la chaussée près de la place des Nations Unies.',
                'latitude' => 33.5731,
                'longitude' => -7.5898,
                'status' => Report::STATUS_PENDING,
            ],
            [
                'title' => 'Éclairage défaillant — Maarif',
                'description' => 'Plusieurs lampadaires éteints rue Ibnou Rochd.',
                'latitude' => 33.5889,
                'longitude' => -7.6324,
                'status' => Report::STATUS_IN_PROGRESS,
            ],
            [
                'title' => 'Déchets accumulés — Ain Sebaâ',
                'description' => 'Poubelles pleines et ordures au sol depuis trois jours.',
                'latitude' => 33.6092,
                'longitude' => -7.5201,
                'status' => Report::STATUS_PENDING,
            ],
            [
                'title' => 'Fuite d\'eau — Hay Hassani',
                'description' => 'Canalisation qui coule sur le trottoir.',
                'latitude' => 33.5445,
                'longitude' => -7.6512,
                'status' => Report::STATUS_PENDING,
            ],
            [
                'title' => 'Panneau routier cassé — Corniche',
                'description' => 'Signalisation renversée après tempête.',
                'latitude' => 33.5982,
                'longitude' => -7.6789,
                'status' => Report::STATUS_RESOLVED,
            ],
        ];

        foreach ($reports as $data) {
            Report::updateOrCreate(
                ['user_id' => $user->id, 'title' => $data['title']],
                [
                    'description' => $data['description'],
                    'latitude' => $data['latitude'],
                    'longitude' => $data['longitude'],
                    'status' => $data['status'],
                    'category' => $classifier->classify($data['title'], $data['description']),
                ]
            );
        }
    }
}
