<?php

namespace App\Services;

class ReportClassifier
{
    /** @var array<string, list<string>> */
    private const KEYWORDS = [
        'infrastructure' => ['pothole', 'road', 'rue', 'trou', 'nid', 'trottoir', 'route', 'asphalte', 'fissure'],
        'lighting' => ['light', 'lamp', 'éclairage', 'eclairage', 'lumière', 'lumiere', 'lampadaire', 'streetlight'],
        'waste' => ['trash', 'waste', 'déchet', 'dechet', 'poubelle', 'ordure', 'garbage', 'salissure'],
        'water' => ['water', 'eau', 'fuite', 'inondation', 'égout', 'egout', 'canalisation'],
        'safety' => ['danger', 'accident', 'signalisation', 'panneau', 'feu', 'traffic'],
    ];

    public function classify(?string $title, ?string $description): string
    {
        $text = mb_strtolower(trim(($title ?? '').' '.($description ?? '')));

        if ($text === '') {
            return 'general';
        }

        $bestCategory = 'general';
        $bestScore = 0;

        foreach (self::KEYWORDS as $category => $keywords) {
            $score = 0;
            foreach ($keywords as $keyword) {
                if (str_contains($text, $keyword)) {
                    $score++;
                }
            }
            if ($score > $bestScore) {
                $bestScore = $score;
                $bestCategory = $category;
            }
        }

        return $bestCategory;
    }
}
