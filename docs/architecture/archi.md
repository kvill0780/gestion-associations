# 🏗️ ARCHITECTURE FINALE - Système de Permissions Associa

📋 Vue d'Ensemble
Architecture à 3 niveaux optimisée pour gestion d'associations étudiantes avec support de mandats électoraux, historique légal et permissions techniques.

Principe Fondamental
POSTES (Titres Statutaires) ─────┐
  "Président", "Trésorier"       │
                                  ├──> MANDATES (Historique Légal)
RÔLES (Permissions Techniques) ───┤      Qui → Quoi → Quand
  "Admin", "Finance Manager"     │
                                  └──> USER_ROLES (Permissions Actives)
Séparation claire :

POSTES = Titres officiels pour PV, AG, organigramme

RÔLES = Permissions techniques (ce qu'on peut faire dans l'app)

MANDATES = Historique "qui occupe quel poste, quelle période"

USER_ROLES = Attribution des permissions opérationnelles

🗄️ SCHÉMA DE BASE DE DONNÉES
1. Table roles
Objectif : Définir les permissions techniques par association

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    association_id BIGINT REFERENCES associations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    permissions JSONB NOT NULL DEFAULT '{}',
    type VARCHAR(20) DEFAULT 'custom',
    is_default BOOLEAN DEFAULT false,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT valid_permissions CHECK (jsonb_typeof(permissions) = 'object')
);

CREATE INDEX idx_roles_association ON roles(association_id);
CREATE INDEX idx_roles_permissions ON roles USING GIN (permissions);
Champs :

permissions : JSONB avec structure {"module.action": true}

type : 'leadership' | 'member' | 'custom'

is_default : Rôle attribué automatiquement aux nouveaux membres

Exemple de permissions JSON :

{
  "members.view": true,
  "members.create": true,
  "finances.view": true,
  "finances.create": true,
  "finances.approve": true,
  "events.view": true,
  "events.create": true
}
2. Table posts
Objectif : Définir les titres statutaires de l'association (Bureau)

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    association_id BIGINT NOT NULL REFERENCES associations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_leadership BOOLEAN DEFAULT true,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    UNIQUE(association_id, name)
);

CREATE INDEX idx_posts_association ON posts(association_id);
Exemples de postes :

Président

Vice-Président

Secrétaire Général

Trésorier

Responsable Communication

Responsable Événements

3. Table post_role (Pivot)
Objectif : Lier un poste à un rôle par défaut (suggestion, pas auto-trigger)

CREATE TABLE post_role (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    UNIQUE(post_id, role_id)
);

CREATE INDEX idx_post_role_post ON post_role(post_id);
CREATE INDEX idx_post_role_role ON post_role(role_id);
Usage :

Quand admin crée un poste "Trésorier", il peut le lier au rôle "Finance Manager"

Lors de l'attribution du poste, l'UI suggère le rôle lié

IMPORTANT : Ce n'est PAS un auto-trigger, juste une suggestion UI

4. Table mandates
Objectif : Historique légal "qui occupe quel poste, quelle période"

CREATE TABLE mandates (
    id BIGSERIAL PRIMARY KEY,
    association_id BIGINT NOT NULL REFERENCES associations(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE,
    assigned_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    active BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    UNIQUE(user_id, post_id, start_date)
);

CREATE INDEX idx_mandates_user ON mandates(user_id);
CREATE INDEX idx_mandates_post ON mandates(post_id);
CREATE INDEX idx_mandates_active ON mandates(active);
CREATE INDEX idx_mandates_dates ON mandates(start_date, end_date);
Champs clés :

active : true si mandat en cours

end_date : NULL si mandat illimité ou en cours

assigned_by : Qui a attribué ce mandat (traçabilité)

5. Table user_roles (Pivot)
Objectif : Attribution des rôles opérationnels aux utilisateurs avec historique

CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    term_start DATE,
    term_end DATE,
    is_active BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    
    UNIQUE(user_id, role_id, is_active)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_user_roles_active ON user_roles(is_active);
Champs clés :

term_start/term_end : Période de validité du rôle (peut correspondre au mandat)

is_active : false si rôle révoqué ou expiré

assigned_by : Traçabilité

🎯 SYSTÈME DE PERMISSIONS
Structure des Permissions
Convention : module.action

// Exemple de permissions (25-30 max)
const PERMISSIONS = {
  // MEMBRES (5)
  'members.view',
  'members.create',
  'members.update',
  'members.delete',
  'members.approve',
  
  // FINANCES (6)
  'finances.view',
  'finances.create',
  'finances.update',
  'finances.delete',
  'finances.approve',
  'finances.export',
  
  // ÉVÉNEMENTS (5)
  'events.view',
  'events.create',
  'events.update',
  'events.delete',
  'events.manage',
  
  // DOCUMENTS (4)
  'documents.view',
  'documents.upload',
  'documents.delete',
  'documents.share',
  
  // ADMINISTRATION (5)
  'roles.manage',
  'posts.manage',
  'settings.view',
  'settings.update',
  'super_admin'
};

Macros (Optionnelles)
// Permissions groupées pour faciliter templates
const MACROS = {
  'members_all': ['members.view', 'members.create', 'members.update', 'members.delete', 'members.approve'],
  'finances_all': ['finances.view', 'finances.create', 'finances.update', 'finances.delete', 'finances.approve', 'finances.export'],
  'events_all': ['events.view', 'events.create', 'events.update', 'events.delete', 'events.manage'],
  'documents_all': ['documents.view', 'documents.upload', 'documents.delete', 'documents.share'],
  'admin_all': ['*'] // Toutes les permissions sauf super_admin
};
Stockage dans roles.permissions :

{
  "finances_all": true,
  "members.view": true,
  "documents.view": true
}
🔐 LOGIQUE DE VÉRIFICATION
Backend (Laravel)
Modèle User
class User extends Model
{
    // Relation : Rôles actifs uniquement
    public function activeRoles()
    {
        return $this->belongsToMany(Role::class, 'user_roles')
                    ->wherePivot('is_active', true);
    }
    
    // Relation : Tous les rôles (historique)
    public function roles()
    {
        return $this->belongsToMany(Role::class, 'user_roles')
                    ->withPivot('is_active', 'term_start', 'term_end', 'assigned_by', 'assigned_at')
                    ->withTimestamps();
    }
    
    // Relation : Mandats actifs
    public function activeMandates()
    {
        return $this->hasMany(Mandate::class)->where('active', true);
    }
    
    // Vérification de permission
    public function hasPermission(string $permission): bool
    {
        // 1. Super admin bypass
        if ($this->is_super_admin) {
            return true;
        }
        
        // 2. Vérifier dans tous les rôles actifs
        foreach ($this->activeRoles as $role) {
            $perms = $role->permissions ?? [];
            
            // 3. Admin all bypass
            if (isset($perms['admin_all']) && $perms['admin_all'] === true) {
                return true;
            }
            
            // 4. Vérifier macro (module_all)
            [$module] = explode('.', $permission, 2) + [null, null];
            if ($module && isset($perms["{$module}_all"]) && $perms["{$module}_all"] === true) {
                return true;
            }
            
            // 5. Vérifier permission directe
            if (isset($perms[$permission]) && $perms[$permission] === true) {
                return true;
            }
        }
        
        return false;
    }
    
    // Helper : Vérifier plusieurs permissions
    public function hasAnyPermission(array $permissions): bool
    {
        foreach ($permissions as $permission) {
            if ($this->hasPermission($permission)) {
                return true;
            }
        }
        return false;
    }
    
    public function hasAllPermissions(array $permissions): bool
    {
        foreach ($permissions as $permission) {
            if (!$this->hasPermission($permission)) {
                return false;
            }
        }
        return true;
    }
}

Middleware Permission
class CheckPermission
{
    public function handle($request, Closure $next, $permission)
    {
        if (!auth()->user()->hasPermission($permission)) {
            abort(403, 'Action non autorisée.');
        }
        
        return $next($request);
    }
}

// Utilisation dans routes
Route::get('/finances', [FinanceController::class, 'index'])
    ->middleware(['auth:sanctum', 'permission:finances.view']);
Frontend (React)
Hook usePermissions
// hooks/usePermissions.js
import { useAuth } from '@/contexts/AuthContext';

export const usePermissions = () => {
  const { user } = useAuth();
  
  const can = (permission) => {
    if (!user) return false;
    
    // Super admin bypass
    if (user.is_super_admin) return true;
    
    // Vérifier dans tous les rôles actifs
    return user.roles?.some(role => {
      const perms = role.permissions || {};
      
      // Admin all bypass
      if (perms.admin_all === true) return true;
      
      // Macro check
      const [module] = permission.split('.');
      if (perms[`${module}_all`] === true) return true;
      
      // Direct check
      return perms[permission] === true;
    }) || false;
  };
  
  const canAny = (permissions) => {
    return permissions.some(p => can(p));
  };
  
  const canAll = (permissions) => {
    return permissions.every(p => can(p));
  };
  
  return { can, canAny, canAll };
};

Utilisation dans Composants
import { usePermissions } from '@/hooks/usePermissions';

const FinancesPage = () => {
  const { can } = usePermissions();
  
  return (
    <div>
      {can('finances.view') && (
        <TransactionsList />
      )}
      
      {can('finances.create') && (
        <Button onClick={handleCreate}>
          Nouvelle Transaction
        </Button>
      )}
      
      {can('finances.approve') && (
        <ApprovalSection />
      )}
    </div>
  );
};
🔄 WORKFLOW D'ATTRIBUTION
Scénario : Nommer Jean comme Trésorier
Backend Endpoint Atomique
// POST /api/v1/associations/{id}/assign-post
// MandateController.php

public function assignPost(AssignPostRequest $request, Association $association)
{
    return DB::transaction(function () use ($request, $association) {
        
        // 1. Désactiver mandat précédent sur ce poste (si existe)
        Mandate::where('post_id', $request->post_id)
               ->where('active', true)
               ->update([
                   'active' => false,
                   'end_date' => now()
               ]);
        
        // 2. Créer nouveau mandat
        $mandate = Mandate::create([
            'association_id' => $association->id,
            'user_id' => $request->user_id,
            'post_id' => $request->post_id,
            'start_date' => $request->start_date,
            'end_date' => $request->end_date,
            'assigned_by' => auth()->id(),
            'active' => true,
            'notes' => $request->notes
        ]);
        
        // 3. Attribuer rôle (si demandé)
        if ($request->assign_role) {
            // Récupérer rôle par défaut ou override
            $roleId = $request->role_override_id ?? PostRole::where('post_id', $request->post_id)->value('role_id');
            
            if ($roleId) {
                // Désactiver ancien rôle similaire (optionnel)
                UserRole::where('user_id', $request->user_id)
                        ->where('role_id', $roleId)
                        ->update(['is_active' => false]);
                
                // Créer nouveau user_role
                UserRole::create([
                    'user_id' => $request->user_id,
                    'role_id' => $roleId,
                    'term_start' => $request->start_date,
                    'term_end' => $request->end_date,
                    'is_active' => true,
                    'assigned_by' => auth()->id(),
                    'assigned_at' => now()
                ]);
            }
        }
        
        // 4. Logger l'action (audit)
        AuditLog::create([
            'action' => 'assign_post',
            'user_id' => $request->user_id,
            'post_id' => $request->post_id,
            'role_id' => $roleId ?? null,
            'performed_by' => auth()->id(),
            'metadata' => $request->all()
        ]);
        
        // 5. Envoyer notification
        $user = User::find($request->user_id);
        $user->notify(new PostAssignedNotification($mandate));
        
        return new MandateResource($mandate->load('user', 'post'));
    });
}

Request Validation
// AssignPostRequest.php
class AssignPostRequest extends FormRequest
{
    public function rules()
    {
        return [
            'user_id' => 'required|exists:users,id',
            'post_id' => 'required|exists:posts,id',
            'start_date' => 'required|date',
            'end_date' => 'nullable|date|after:start_date',
            'assign_role' => 'boolean',
            'role_override_id' => 'nullable|exists:roles,id',
            'notes' => 'nullable|string|max:500'
        ];
    }
    
    public function authorize()
    {
        return auth()->user()->hasPermission('posts.manage');
    }
}
Frontend API Call
// services/mandatesService.js
export const mandatesService = {
  assignPost: async (associationId, data) => {
    const response = await apiClient.post(
      `/associations/${associationId}/assign-post`,
      {
        user_id: data.userId,
        post_id: data.postId,
        start_date: data.startDate,
        end_date: data.endDate,
        assign_role: data.assignRole,
        role_override_id: data.roleOverrideId,
        notes: data.notes
      }
    );
    return response.data;
  }
};
UI Component
// AssignPostModal.jsx
const AssignPostModal = ({ isOpen, onClose, associationId }) => {
  const { data: posts } = usePosts(associationId);
  const { data: users } = useMembers(associationId);
  const assignPost = useAssignPost();
  
  const { register, handleSubmit, watch } = useForm();
  const selectedPostId = watch('post_id');
  
  // Récupérer rôle suggéré
  const { data: suggestedRole } = useQuery(
    ['post-role', selectedPostId],
    () => rolesService.getSuggestedRole(selectedPostId),
    { enabled: !!selectedPostId }
  );
  
  const onSubmit = (data) => {
    assignPost.mutate({
      associationId,
      ...data,
      assign_role: data.assign_role ?? true
    }, {
      onSuccess: () => {
        toast.success('Mandat attribué avec succès');
        onClose();
      }
    });
  };
  
  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Attribuer un poste">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        
        <Select
          label="Membre"
          {...register('user_id', { required: true })}
          options={users?.map(u => ({ value: u.id, label: `${u.first_name} ${u.last_name}` }))}
        />
        
        <Select
          label="Poste"
          {...register('post_id', { required: true })}
          options={posts?.map(p => ({ value: p.id, label: p.name }))}
        />
        
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Date début"
            type="date"
            {...register('start_date', { required: true })}
          />
          <Input
            label="Date fin"
            type="date"
            {...register('end_date')}
          />
        </div>
        
        {suggestedRole && (
          <div className="rounded-lg border bg-blue-50 p-4">
            <label className="flex items-center space-x-2">
              <input
                type="checkbox"
                {...register('assign_role')}
                defaultChecked={true}
              />
              <span>
                Attribuer automatiquement le rôle <strong>{suggestedRole.name}</strong>
              </span>
            </label>
            <p className="mt-2 text-sm text-gray-600">
              Ce rôle donne accès : {suggestedRole.permissions_count} permissions
            </p>
          </div>
        )}
        
        <Textarea
          label="Notes (optionnel)"
          {...register('notes')}
          placeholder="Raison de l'attribution, contexte..."
        />
        
        <div className="flex justify-end space-x-3">
          <Button variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={assignPost.isPending}>
            {assignPost.isPending ? 'Attribution...' : 'Attribuer'}
          </Button>
        </div>
        
      </form>
    </Modal>
  );
};

📚 TEMPLATES DE RÔLES
Définition des Templates
// config/role_templates.php
return [
    'president' => [
        'name' => 'Président',
        'slug' => 'president',
        'type' => 'leadership',
        'description' => 'Accès complet à toutes les fonctionnalités',
        'permissions' => [
            'admin_all' => true
        ]
    ],
    
    'treasurer' => [
        'name' => 'Trésorier',
        'slug' => 'treasurer',
        'type' => 'leadership',
        'description' => 'Gestion complète des finances',
        'permissions' => [
            'finances_all' => true,
            'members.view' => true,
            'documents.view' => true,
            'documents.upload' => true
        ]
    ],
    
    'secretary' => [
        'name' => 'Secrétaire Général',
        'slug' => 'secretary',
        'type' => 'leadership',
        'description' => 'Gestion administrative et membres',
        'permissions' => [
            'members_all' => true,
            'documents_all' => true,
            'events.view' => true,
            'events.create' => true
        ]
    ],
    
    'event_manager' => [
        'name' => 'Responsable Événements',
        'slug' => 'event_manager',
        'type' => 'leadership',
        'description' => 'Création et gestion des événements',
        'permissions' => [
            'events_all' => true,
            'members.view' => true,
            'documents.view' => true
        ]
    ],
    
    'member' => [
        'name' => 'Membre',
        'slug' => 'member',
        'type' => 'member',
        'description' => 'Accès de base pour les membres',
        'permissions' => [
            'members.view' => true,
            'events.view' => true,
            'documents.view' => true
        ]
    ]
];

Seeder
// RoleSeeder.php
class RoleSeeder extends Seeder
{
    public function run()
    {
        $templates = config('role_templates');
        
        // Pour chaque association
        Association::all()->each(function ($association) use ($templates) {
            foreach ($templates as $template) {
                Role::create([
                    'association_id' => $association->id,
                    'name' => $template['name'],
                    'slug' => $template['slug'],
                    'permissions' => $template['permissions'],
                    'type' => $template['type'],
                    'is_default' => $template['slug'] === 'member'
                ]);
            }
        });
    }
}
🧪 TESTS
Tests Unitaires
// tests/Unit/UserPermissionTest.php
class UserPermissionTest extends TestCase
{
    /** @test */
    public function user_with_admin_all_has_any_permission()
    {
        $user = User::factory()->create();
        $role = Role::factory()->create([
            'permissions' => ['admin_all' => true]
        ]);
        $user->roles()->attach($role, ['is_active' => true]);
        
        $this->assertTrue($user->hasPermission('finances.view'));
        $this->assertTrue($user->hasPermission('members.delete'));
        $this->assertTrue($user->hasPermission('any.permission'));
    }
    
    /** @test */
    public function user_with_macro_permission_has_module_permissions()
    {
        $user = User::factory()->create();
        $role = Role::factory()->create([
            'permissions' => ['finances_all' => true]
        ]);
        $user->roles()->attach($role, ['is_active' => true]);
        
        $this->assertTrue($user->hasPermission('finances.view'));
        $this->assertTrue($user->hasPermission('finances.create'));
        $this->assertTrue($user->hasPermission('finances.approve'));
        $this->assertFalse($user->hasPermission('members.view'));
    }
    
    /** @test */
    public function user_with_specific_permission_has_only_that_permission()
    {
        $user = User::factory()->create();
        $role = Role::factory()->create([
            'permissions' => [
                'finances.view' => true,
                'members.view' => true
            ]
        ]);
        $user->roles()->attach($role, ['is_active' => true]);
        
        $this->assertTrue($user->hasPermission('finances.view'));
        $this->assertTrue($user->hasPermission('members.view'));
        $this->assertFalse($user->hasPermission('finances.create'));
        $this->assertFalse($user->hasPermission('members.delete'));
    }
    
    /** @test */
    public function inactive_role_permissions_are_not_checked()
    {
        $user = User::factory()->create();
        $role = Role::factory()->create([
            'permissions' => ['admin_all' => true]
        ]);
        $user->roles()->attach($role, ['is_active' => false]);
        
        $this->assertFalse($user->hasPermission('finances.view'));
    }
}

Tests d'Intégration
// tests/Feature/AssignPostTest.php
class AssignPostTest extends TestCase
{
    /** @test */
    public function can_assign_post_with_role()
    {
        $admin = User::factory()->create();
        $role = Role::factory()->create(['permissions' => ['posts.manage' => true]]);
        $admin->roles()->attach($role, ['is_active' => true]);
        
        $user = User::factory()->create();
        $post = Post::factory()->create(['name' => 'Trésorier']);
        $financeRole = Role::factory()->create(['slug' => 'treasurer']);
        PostRole::create(['post_id' => $post->id, 'role_id' => $financeRole->id]);
        
        $response = $this->actingAs($admin)
            ->postJson("/api/v1/associations/{$post->association_id}/assign-post", [
                'user_id' => $user->id,
                'post_id' => $post->id,
                'start_date' => '2025-01-01',
                'end_date' => '2025-12-31',
                'assign_role' => true
            ]);
        
        $response->assertStatus(201);
        
        $this->assertDatabaseHas('mandates', [
            'user_id' => $user->id,
            'post_id' => $post->id,
            'active' => true
        ]);
        
        $this->assertDatabaseHas('user_roles', [
            'user_id' => $user->id,
            'role_id' => $financeRole->id,
            'is_active' => true
        ]);
        
        $user->refresh();
        $this->assertTrue($user->hasPermission('finances.view'));
    }
    
    /** @test */
    public function previous_mandate_is_deactivated()
    {
        $admin = User::factory()->create();
        $role = Role::factory()->create(['permissions' => ['posts.manage' => true]]);
        $admin->roles()->attach($role, ['is_active' => true]);
        
        $oldUser = User::factory()->create();
        $newUser = User::factory()->create();
        $post = Post::factory()->create(['name' => 'Trésorier']);
        
        // Ancien mandat
        Mandate::create([
            'association_id' => $post->association_id,
            'user_id' => $oldUser->id,
            'post_id' => $post->id,
            'start_date' => '2024-01-01',
            'active' => true
        ]);
        
        // Nouveau mandat
        $response = $this->actingAs($admin)
            ->postJson("/api/v1/associations/{$post->association_id}/assign-post", [
                'user_id' => $newUser->id,
                'post_id' => $post->id,
                'start_date' => '2025-01-01',
                'assign_role' => false
            ]);
        
        $response->assertStatus(201);
        
        $this->assertDatabaseHas('mandates', [
            'user_id' => $oldUser->id,
            'post_id' => $post->id,
            'active' => false
        ]);
        
        $this->assertDatabaseHas('mandates', [
            'user_id' => $newUser->id,
            'post_id' => $post->id,
            'active' => true
        ]);
    }
}

🚀 ORDRE DE MIGRATION
1. create_cache_table
2. create_jobs_table
3. create_associations_table
4. create_users_table
5. add_association_id_to_users
6. create_roles_table
7. create_posts_table
8. create_post_role_table
9. create_user_roles_table
10. create_mandates_table
11. create_transactions_table
12. create_events_table
13. create_event_participations_table
14. create_documents_table
15. create_notifications_table
16. seed_default_roles
17. seed_default_posts
📊 AUDIT & LOGGING
Table AuditLog
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    performed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    metadata JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_target ON audit_logs(target_type, target_id);
Actions à Logger
// À logger systématiquement
const AUDIT_ACTIONS = [
    'assign_post',      // Attribution d'un poste
    'revoke_post',      // Révocation d'un poste
    'assign_role',      // Attribution d'un rôle
    'revoke_role',      // Révocation d'un rôle
    'create_role',      // Création rôle
    'update_role',      // Modification permissions
    'delete_role',      // Suppression rôle
    'approve_member',   // Approbation membre
    'approve_transaction', // Approbation transaction
    'delete_user',      // Suppression utilisateur
];
🔄 MIGRATION DEPUIS ANCIEN SYSTÈME
Si tu veux simplifier plus tard
// Script de migration (à exécuter qu'une fois)
class ConsolidatePostsAndRoles extends Command
{
    public function handle()
    {
        DB::transaction(function() {
            
            // 1. Pour chaque mandat actif
            Mandate::where('active', true)->each(function($mandate) {
                
                // 2. Récupérer le rôle lié au poste
                $roleId = PostRole::where('post_id', $mandate->post_id)
                                  ->value('role_id');
                
                if ($roleId) {
                    // 3. Créer user_role si pas existe
                    UserRole::updateOrCreate(
                        [
                            'user_id' => $mandate->user_id,
                            'role_id' => $roleId
                        ],
                        [
                            'term_start' => $mandate->start_date,
                            'term_end' => $mandate->end_date,
                            'is_active' => true,
                            'assigned_at' => $mandate->created_at,
                            'assigned_by' => $mandate->assigned_by
                        ]
                    );
                    
                    $this->info("Migrated: User {$mandate->user_id} -> Role {$roleId}");
                }
            });
            
            $this->info('Migration completed!');
        });
    }
}

📖 DOCUMENTATION API
Endpoints Clés
# POSTES
POST   /api/v1/associations/{id}/posts
GET    /api/v1/associations/{id}/posts
PUT    /api/v1/associations/{id}/posts/{postId}
DELETE /api/v1/associations/{id}/posts/{postId}

# RÔLES
POST   /api/v1/associations/{id}/roles
GET    /api/v1/associations/{id}/roles
PUT    /api/v1/associations/{id}/roles/{roleId}
DELETE /api/v1/associations/{id}/roles/{roleId}

# LIAISON POSTE-RÔLE
POST   /api/v1/associations/{id}/post-role
DELETE /api/v1/associations/{id}/post-role/{postId}/{roleId}

# MANDATS (Attribution atomique)
POST   /api/v1/associations/{id}/assign-post
GET    /api/v1/associations/{id}/mandates
PUT    /api/v1/associations/{id}/mandates/{id}/revoke

# ATTRIBUTION RÔLE MANUEL
POST   /api/v1/users/{id}/roles
DELETE /api/v1/users/{id}/roles/{roleId}

# SYSTÈME
GET    /api/v1/system/permissions
GET    /api/v1/system/permissions/grouped
GET    /api/v1/system/role-templates

Exemple Payload : assign-post
POST /api/v1/associations/5/assign-post

{
  "user_id": 42,
  "post_id": 8,
  "start_date": "2025-01-01",
  "end_date": "2025-12-31",
  "assign_role": true,
  "role_override_id": null,
  "notes": "Élu lors de l'AG du 15/12/2024"
}

Response 201:
{
  "message": "Mandat attribué avec succès",
  "data": {
    "id": 123,
    "user": {
      "id": 42,
      "first_name": "Jean",
      "last_name": "Dupont",
      "email": "jean@example.com"
    },
    "post": {
      "id": 8,
      "name": "Trésorier"
    },
    "role_assigned": {
      "id": 15,
      "name": "Finance Manager"
    },
    "start_date": "2025-01-01",
    "end_date": "2025-12-31",
    "active": true
  }
}

🎨 INTERFACE UTILISATEUR
Écran Principal : Bureau de l'Association
┌──────────────────────────────────────────────────────────┐
│  Bureau 2024-2025                         [+ Nommer]     │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Président                                           │ │
│  │ ┌─────┐  Jean Dupont                               │ │
│  │ │ JD  │  jean.dupont@example.com                   │ │
│  │ └─────┘  Mandat: 01/01/2024 - 31/12/2024           │ │
│  │          Rôle: Administrateur                       │ │
│  │          [Révoquer] [Modifier]                      │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Trésorier                                           │ │
│  │ ┌─────┐  Marie Martin                              │ │
│  │ │ MM  │  marie.martin@example.com                  │ │
│  │ └─────┘  Mandat: 01/01/2024 - 31/12/2024           │ │
│  │          Rôle: Finance Manager                      │ │
│  │          [Révoquer] [Modifier]                      │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Secrétaire Général                                  │ │
│  │          Poste vacant                               │ │
│  │          [Nommer quelqu'un]                         │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                           │
└──────────────────────────────────────────────────────────┘

Modal : Nommer au Bureau
┌────────────────────────────────────────────────┐
│  Attribuer un poste                      [X]   │
├────────────────────────────────────────────────┤
│                                                │
│  Membre *                                      │
│  [Sélectionner...          ▼]                 │
│                                                │
│  Poste *                                       │
│  [Trésorier                ▼]                 │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │ ✓ Attribuer automatiquement le rôle      │ │
│  │   "Finance Manager"                      │ │
│  │                                          │ │
│  │   Ce rôle donne accès à :                │ │
│  │   • Voir finances                        │ │
│  │   • Créer transactions                   │ │
│  │   • Approuver transactions               │ │
│  │   • Exporter rapports                    │ │
│  │   • Voir membres                         │ │
│  │   ... et 3 autres permissions            │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  Période du mandat                             │
│  Du   [01/01/2025]   Au   [31/12/2025]        │
│                                                │
│  Notes (optionnel)                             │
│  [Élu lors de l'AG du 15/12/2024...]          │
│                                                │
│                    [Annuler]  [Attribuer]     │
└────────────────────────────────────────────────┘

Écran : Gestion des Rôles
┌──────────────────────────────────────────────────────────┐
│  Rôles & Permissions                  [+ Créer un rôle]  │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  [Administrateur]                               3 users  │
│  Accès complet à toutes les fonctionnalités             │
│  • admin_all                                             │
│  [Modifier] [Attribuer]                                  │
│                                                           │
│  [Trésorier]                                    1 user   │
│  Gestion complète des finances                           │
│  • finances_all                                          │
│  • members.view                                          │
│  • documents.view, documents.upload                      │
│  [Modifier] [Attribuer]                                  │
│                                                           │
│  [Secrétaire Général]                          1 user   │
│  Gestion administrative et membres                       │
│  • members_all                                           │
│  • documents_all                                         │
│  • events.view, events.create                            │
│  [Modifier] [Attribuer]                                  │
│                                                           │
│  [Membre]                                      45 users  │
│  Accès de base pour les membres                          │
│  • members.view                                          │
│  • events.view                                           │
│  • documents.view                                        │
│  [Modifier] [Attribuer]                                  │
│                                                           │
└──────────────────────────────────────────────────────────┘

✅ CHECKLIST DE MISE EN ŒUVRE
Phase 1 : Base de Données
 Créer migrations dans l'ordre (voir section ordre)
 Ajouter contraintes CHECK sur JSONB (PostgreSQL)
 Créer indexes (GIN pour JSONB, standards pour FK)
 Tester migrations up/down
Phase 2 : Modèles & Relations
 Créer modèles Eloquent (Role, Post, Mandate, PostRole)
 Définir relations belongsToMany, hasMany
 Implémenter méthode hasPermission() dans User
 Créer scopes (activeRoles, activeMandates)
Phase 3 : Business Logic
 Créer Service MandateService (assignPost, revokePost)
 Créer Service RoleService (createRole, updatePermissions)
 Implémenter validation permissions (ValidPermissions rule)
 Créer middleware CheckPermission
Phase 4 : API Endpoints
 Endpoint POST /assign-post (atomique)
 CRUD Postes
 CRUD Rôles
 Endpoint liaison post-role
 Endpoint attribution rôle manuel
Phase 5 : Frontend
 Hook usePermissions
 Component AssignPostModal
 Component RoleManageModal
 Component BureauList (affichage mandats)
 Component PermissionsEditor
Phase 6 : Tests
 Tests unitaires hasPermission()
 Tests d'intégration assignPost
 Tests permissions hiérarchie (admin_all, macros)
 Tests révocation mandats
Phase 7 : Seeders & Fixtures
 Seeder role_templates
 Seeder posts standards
 Seeder test users avec différents rôles
Phase 8 : Documentation
 Documenter endpoints API (Swagger)
 Guide admin : "Comment gérer le bureau"
 Guide développeur : "Ajouter une nouvelle permission"
Phase 9 : Audit & Sécurité
 Implémenter AuditLog
 Logger toutes actions sensibles
 Vérifier permissions sur tous endpoints
 Tests de sécurité (accès non autorisés)
Phase 10 : Production
 Migration données (si existant)
 Tests charge (permissions)
 Monitoring (requêtes lentes sur JSONB)
 Documentation utilisateur finale
🎯 POINTS CRITIQUES À RETENIR
✅ DOs
Transaction DB pour assign-post (mandate + user_role ensemble)

Validation stricte des clés JSON permissions

Logger toutes actions sensibles (AuditLog)

Tester hasPermission() avec tous cas (admin_all, macros, direct)

Index GIN sur roles.permissions (PostgreSQL)

is_active pour historique (ne pas supprimer, désactiver)

❌ DON'Ts
Jamais auto-trigger rôle depuis poste (explicite uniquement)

Jamais modifier permissions d'un rôle avec users actifs sans prévenir

Jamais supprimer mandates/user_roles (soft delete ou is_active)

Jamais bypass hasPermission() dans code métier

Jamais stocker permissions dans user directement (toujours via roles)

📞 SUPPORT & ÉVOLUTION
Ajout d'une Nouvelle Permission
Ajouter dans config permissions

'module.new_action' => true
Mettre à jour role_templates.php si nécessaire

Créer migration data pour ajouter aux rôles existants

Role::where('slug', 'admin')->each(function($role) {
    $perms = $role->permissions;
    $perms['module.new_action'] = true;
    $role->update(['permissions' => $perms]);
});
Ajouter tests

Ajout d'un Nouveau Module
Créer permissions groupées

'new_module.view' => true,
'new_module.create' => true,
'new_module.update' => true,
'new_module.delete' => true,
Créer macro optionnelle

'new_module_all' => true
Mettre à jour documentation

🔧 TROUBLESHOOTING
Permission non reconnue
// Vérifier que permission existe dans role
$user->activeRoles->first()->permissions;

// Vérifier format (module.action)
if (!str_contains($permission, '.')) {
    throw new InvalidArgumentException('Format invalide');
}
Mandat créé mais pas de rôle
// Vérifier transaction DB
DB::transaction(function() {
    // Code ici
});

// Vérifier logs
Log::info('Assigning role', ['user_id' => $userId, 'role_id' => $roleId]);
Performance JSONB lente
-- Créer index
CREATE INDEX idx_roles_permissions ON roles USING GIN (permissions);

-- Vérifier query plan
EXPLAIN ANALYZE 
SELECT * FROM roles WHERE permissions @> '{"finances.view": true}';
📚 RESSOURCES
Laravel Eloquent Relationships: https://laravel.com/docs/eloquent-relationships

PostgreSQL JSONB: https://www.postgresql.org/docs/current/datatype-json.html

React Query: https://tanstack.com/query/latest

Tailwind CSS: https://tailwindcss.com/docs

Version: 1.0.0
Date: Décembre 2024
Auteur: Architecture Associa
Status: Production Ready ✅