@extends('admin.layout')

@section('title', 'Connexion Admin')

@section('body')
<div class="login-page">
    <div class="glass-card login-card">
        <h1>Smart City <span style="color: var(--accent)">Admin</span></h1>
        <p>Tableau de bord municipal — gestion des signalements citoyens</p>

        @if ($errors->any())
            <div class="form-error">{{ $errors->first() }}</div>
        @endif

        <form method="POST" action="{{ route('admin.login.submit') }}">
            @csrf
            <div class="form-group">
                <label for="email">E-mail</label>
                <input id="email" type="email" name="email" value="{{ old('email') }}" required autofocus>
            </div>
            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input id="password" type="password" name="password" required>
            </div>
            <button type="submit" class="btn-primary" style="width:100%; margin-top:8px;">Se connecter</button>
        </form>

        <p style="margin-top:20px; font-size:0.75rem; color: var(--muted);">
            Démo : admin@smartcity.local / admin123
        </p>
    </div>
</div>
@endsection
