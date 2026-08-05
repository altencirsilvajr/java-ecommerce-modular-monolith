package dev.altencir.ecommerce.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class ReliableEvents {
    private final OutboxRepository outbox;
    private final InboxRepository inbox;
    private final ApplicationEventPublisher events;
    private final ObjectMapper json;

    ReliableEvents(OutboxRepository outbox, InboxRepository inbox, ApplicationEventPublisher events, ObjectMapper json) {
        this.outbox = outbox; this.inbox = inbox; this.events = events; this.json = json;
    }

    public void appendAndPublish(UUID eventId, Object event) {
        outbox.save(new OutboxMessage(eventId, event.getClass().getSimpleName(), json.writeValueAsString(event)));
        events.publishEvent(event);
    }
    public boolean first(UUID eventId, String consumer) {
        String key = consumer + ":" + eventId;
        if (inbox.existsById(key)) return false;
        inbox.save(new InboxMessage(key));
        return true;
    }
}

@Service
@ConditionalOnProperty(name = "ecommerce.outbox.enabled", havingValue = "true", matchIfMissing = true)
class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    private final OutboxRepository outbox;
    private final RabbitTemplate rabbit;
    OutboxDispatcher(OutboxRepository outbox, RabbitTemplate rabbit) { this.outbox = outbox; this.rabbit = rabbit; }

    @Scheduled(fixedDelayString = "${ecommerce.outbox.interval:1000}")
    @Transactional
    public void publish() {
        for (var message : outbox.findTop50ByPublishedAtIsNullOrderByOccurredAtAsc()) {
            try {
                rabbit.convertAndSend("ecommerce.events", message.eventType, message.payload);
                message.published();
            } catch (RuntimeException error) {
                log.warn("outbox_publish_failed eventId={} type={}", message.id, message.eventType);
                break;
            }
        }
    }
}
