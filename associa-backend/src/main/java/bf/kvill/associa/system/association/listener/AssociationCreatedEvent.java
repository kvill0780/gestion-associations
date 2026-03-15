package bf.kvill.associa.system.association.listener;

import bf.kvill.associa.system.association.Association;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Événement émis quand une association est créée
 *
 * Cet événement déclenche la création automatique des rôles templates
 * via AssociationCreatedListener
 */
@Getter
public class AssociationCreatedEvent extends ApplicationEvent {

    private final Association association;

    public AssociationCreatedEvent(Object source, Association association) {
        super(source);
        this.association = association;
    }
}