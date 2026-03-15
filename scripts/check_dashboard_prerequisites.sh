#!/bin/bash

# 🔍 SCRIPT DE VÉRIFICATION - DASHBOARD IMPLEMENTATION
# Vérifie que tous les prérequis sont présents

echo "🔍 Vérification des prérequis pour l'implémentation du Dashboard..."
echo ""

SCRIPT_DIR="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../associa_backend_spring_boot" && pwd)"
ERRORS=0
WARNINGS=0

# Fonction de vérification
check_file() {
    local file=$1
    local name=$2
    
    if [ -f "$PROJECT_ROOT/$file" ]; then
        echo "✅ $name trouvé"
        return 0
    else
        echo "❌ $name MANQUANT"
        ((ERRORS++))
        return 1
    fi
}

check_class() {
    local pattern=$1
    local name=$2
    
    if find "$PROJECT_ROOT/src" -name "$pattern" | grep -q .; then
        echo "✅ $name trouvé"
        return 0
    else
        echo "⚠️  $name NON TROUVÉ (peut être normal)"
        ((WARNINGS++))
        return 1
    fi
}

echo "📦 Vérification des entités principales..."
echo "----------------------------------------"
check_file "src/main/java/bf/kvill/associa/members/user/User.java" "User.java"
check_file "src/main/java/bf/kvill/associa/members/user/UserRepository.java" "UserRepository.java"
check_file "src/main/java/bf/kvill/associa/system/association/Association.java" "Association.java"
check_file "src/main/java/bf/kvill/associa/system/association/AssociationRepository.java" "AssociationRepository.java"
echo ""

echo "💰 Vérification du module Finances..."
echo "----------------------------------------"
check_class "Transaction.java" "Transaction"
check_class "TransactionRepository.java" "TransactionRepository"
echo ""

echo "📅 Vérification du module Events (optionnel)..."
echo "----------------------------------------"
check_class "Event.java" "Event"
check_class "EventRepository.java" "EventRepository"
echo ""

echo "📄 Vérification du module Documents (optionnel)..."
echo "----------------------------------------"
check_class "Document.java" "Document"
check_class "DocumentRepository.java" "DocumentRepository"
echo ""

echo "📋 Vérification du module Audit..."
echo "----------------------------------------"
check_file "src/main/java/bf/kvill/associa/system/audit/AuditLog.java" "AuditLog.java"
check_file "src/main/java/bf/kvill/associa/system/audit/AuditRepository.java" "AuditRepository.java"
echo ""

echo "🔐 Vérification de la sécurité..."
echo "----------------------------------------"
check_file "src/main/java/bf/kvill/associa/security/userdetails/CustomUserPrincipal.java" "CustomUserPrincipal.java"
check_file "src/main/java/bf/kvill/associa/shared/dto/ApiResponse.java" "ApiResponse.java"
echo ""

echo "📊 Vérification du Dashboard existant..."
echo "----------------------------------------"
check_file "src/main/java/bf/kvill/associa/dashboard/DashboardController.java" "DashboardController (existant)"
check_file "src/main/java/bf/kvill/associa/dashboard/DashboardService.java" "DashboardService (existant)"
echo ""

echo "═══════════════════════════════════════"
echo "📊 RÉSUMÉ"
echo "═══════════════════════════════════════"
echo "❌ Erreurs critiques: $ERRORS"
echo "⚠️  Avertissements: $WARNINGS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "✅ Tous les prérequis critiques sont présents !"
    echo "📝 Vous pouvez suivre le guide docs/dashboard/DASHBOARD_IMPLEMENTATION_GUIDE.md"
    echo ""
    echo "⚠️  Note: Les modules Event et Document sont optionnels."
    echo "   Si absents, commentez les sections correspondantes dans le service."
    exit 0
else
    echo "❌ Il manque $ERRORS fichier(s) critique(s)"
    echo "📝 Veuillez créer les fichiers manquants avant de continuer"
    exit 1
fi
