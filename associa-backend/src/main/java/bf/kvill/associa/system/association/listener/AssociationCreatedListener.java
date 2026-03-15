package bf.kvill.associa.system.association.listener;

import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.core.config.RoleTemplatesConfig;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener qui crée automatiquement les rôles templates
 * quand une association est créée
 */
@Component
@RequiredArgsConstructor
public class AssociationCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(AssociationCreatedListener.class);

    private final RoleRepository roleRepository;

    /**
     * Méthode appelée automatiquement quand une AssociationCreatedEvent est émis
     *
     * Crée les 6 rôles templates :
     * 1. Président (admin_all)
     * 2. Trésorier (finances_all)
     * 3. Secrétaire Général (members_all + documents_all)
     * 4. Responsable Événements (events_all)
     * 5. Responsable Communication (announcements_all + messages_all)
     * 6. Membre (permissions de base)
     */
    @EventListener
    @Transactional
    public void onAssociationCreated(AssociationCreatedEvent event) {
        Association association = event.getAssociation();

        log.info("Création des rôles templates pour l'association: {}", association.getName());

        int successCount = 0;
        int errorCount = 0;

        for (var template : RoleTemplatesConfig.TEMPLATES) {
            try {
                Role role = Role.builder()
                        .association(association)
                        .name(template.name())
                        .slug(template.slug())
                        .description(template.description())
                        .type(template.type())
                        .permissions(template.permissions())
                        .isTemplate(template.isTemplate())
                        .displayOrder(template.displayOrder())
                        .build();

                roleRepository.save(role);
                successCount++;

                log.debug("Rôle créé: {} pour {}", template.name(), association.getName());

            } catch (Exception e) {
                errorCount++;
                log.error("Erreur création rôle '{}': {}", template.name(), e.getMessage());
            }
        }

        log.info("Création rôles terminée: {} succès, {} erreurs", successCount, errorCount);
    }
}
