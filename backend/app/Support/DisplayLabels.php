<?php

namespace App\Support;

/**
 * Libellés français pour l'affichage des valeurs API (catégories IA, statuts).
 * Les clés en base / JSON restent en anglais.
 */
final class DisplayLabels
{
    private const CATEGORIES = [
        'safety' => 'Sécurité',
        'infrastructure' => 'Infrastructure',
        'lighting' => 'Éclairage',
        'waste' => 'Déchets',
        'water' => 'Eau',
        'general' => 'Général',
    ];

    private const STATUSES = [
        'pending' => 'En attente',
        'in_progress' => 'En cours',
        'resolved' => 'Résolu',
    ];

    private function __construct() {}

    public static function category(?string $apiCategory): string
    {
        if ($apiCategory === null || $apiCategory === '') {
            return self::CATEGORIES['general'];
        }

        return self::CATEGORIES[strtolower($apiCategory)] ?? self::CATEGORIES['general'];
    }

    public static function status(?string $apiStatus): string
    {
        if ($apiStatus === null || $apiStatus === '') {
            return self::STATUSES['pending'];
        }

        return self::STATUSES[strtolower($apiStatus)] ?? self::STATUSES['pending'];
    }
}
