package br.edu.infnet.ecommerce.pagamento.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class EventoDominio {

    private final String eventId;
    private final LocalDateTime ocorridoEm;

    protected EventoDominio() {
        this.eventId = UUID.randomUUID().toString();
        this.ocorridoEm = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public abstract String tipo();
}
