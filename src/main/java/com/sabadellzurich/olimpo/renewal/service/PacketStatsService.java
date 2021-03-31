package com.sabadellzurich.olimpo.renewal.service;

import com.sabadellzurich.olimpo.renewal.common.lib.enumstatus.ChannelAssignedEnum;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.dto.FullPacketDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Packet stats service.
 * =====================
 * <p>
 * Provides the queries to obtain a certain packet's stats.
 *
 * @author msellers
 */
@Service
@Slf4j
public class PacketStatsService {
    @Autowired
    private EntityManager em;

    public void setStats(FullPacketDTO dto) {
        log.info("Setting postal");
        var postal = this.getStats(dto.getId(), ChannelAssignedEnum.POSTAL_ASSIGNED.toString());
        dto.getStats().put(ChannelAssignedEnum.POSTAL_ASSIGNED, postal);

        log.info("Setting mail");
        var mail = this.getStats(
                dto.getId(),
                ChannelAssignedEnum.MAIL_ASSIGNED.toString(),
                "mailSend",
                "mailComplaint"
        );
        dto.getStats().put(ChannelAssignedEnum.MAIL_ASSIGNED, mail);

        log.info("Setting sms");
        var sms = this.getStats(
                dto.getId(),
                ChannelAssignedEnum.SMS_ASSIGNED.toString(),
                "smsSend",
                "smsTechError"
        );
        dto.getStats().put(ChannelAssignedEnum.SMS_ASSIGNED, sms);

        log.info("Done");
    }

    private FullPacketDTO.Stats getStats(long id, String channelAssigned) {
        var builder = em.getCriteriaBuilder();
        var stats = new FullPacketDTO.Stats();

        var total = builder.createQuery(Long.class);
        var totalRoot = total.from(RenewalCommunicationItem.class);
        total.select(builder.count(totalRoot));
        total.where(where(builder, totalRoot, id, channelAssigned));

        stats.setTotal(em.createQuery(total).getSingleResult());

        var notSent = stats.getTotal() - stats.getSent();
        stats.setNotSent(notSent < 0 ? 0 : notSent);

        return stats;
    }

    private FullPacketDTO.Stats getStats(long id, String channelAssigned, String sentColumn, String errorColumn) {
        var stats = getStats(id, channelAssigned);

        stats.setSent(sum(id, channelAssigned, sentColumn));
        stats.setError(sum(id, channelAssigned, errorColumn));
        stats.setLanding(sum(id, channelAssigned, "landingOpen"));

        var notSent = stats.getTotal() - stats.getSent();
        stats.setNotSent(notSent < 0 ? 0 : notSent);

        return stats;
    }

    private Predicate where(CriteriaBuilder builder, Root<RenewalCommunicationItem> root, long id, String channelAssigned) {
        return builder.and(
                builder.equal(root.get("renewalCommunicationPacket"), id),
                builder.equal(root.get("channelAssigned"), channelAssigned)
        );
    }

    private Integer sum(long id, String channelAssigned, String column) {
        var builder = em.getCriteriaBuilder();

        var sent = builder.createQuery(Integer.class);
        var sentRoot = sent.from(RenewalCommunicationItem.class);
        sent.select(builder.sum(sentRoot.get(column)));

        sent.where(where(builder, sentRoot, id, channelAssigned));

        var ret = em.createQuery(sent).getSingleResult();
        if (ret == null) {
            return 0;
        }

        return ret;
    }
}
