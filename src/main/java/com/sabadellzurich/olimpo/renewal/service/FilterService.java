package com.sabadellzurich.olimpo.renewal.service;

import com.sabadellzurich.olimpo.renewal.common.lib.criteria.ItemCriteria;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemFilterRequest;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.common.lib.services.RecomhService;
import com.sabadellzurich.olimpo.renewal.common.lib.services.impl.AdvancedQueryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Filter service.
 * ===============
 * <p>
 * Offers item and package filtering service based on CriteriaDSL.
 *
 * @author msellers
 */
@Service
public class FilterService {
    @Autowired
    private AdvancedQueryServiceImpl queryService;

    @Autowired
    private RecomhService recomhService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Performs the filter query on the items table.
     *
     * @param f Filter to apply.
     * @return Filtered items;
     */
    public Page<RenewalCommunicationItem> items(ItemFilterRequest f, Pageable pagination) {
        return queryService.paginate(
                this.getFilterCriteria(f),
                pagination,
                entityManager,
                RenewalCommunicationItem.class
        );
    }

    /**
     * Performs the filter query on the items table.
     *
     * @param f Filter to apply.
     * @return Filtered items stream;
     */
    public Stream<RenewalCommunicationItem> items(ItemFilterRequest f) {
        return queryService.stream(
                this.getFilterCriteria(f),
                entityManager,
                RenewalCommunicationItem.class
        );
    }

    /**
     * Performs the filter query on the items table.
     *
     * @param id Packet id.
     * @return Items from the given packet id as a stream.
     */
    public Stream<RenewalCommunicationItem> items(long id) {
        return queryService.stream(
                Collections.singletonList(ItemCriteria.renewalCommunicationPacket(id)),
                entityManager,
                RenewalCommunicationItem.class
        );
    }

    private List<Specification<RenewalCommunicationItem>> getFilterCriteria(ItemFilterRequest f) {
        var file = ItemCriteria.fileName(f.getFileName());
        var id = ItemCriteria.renewalCommunicationPacket(f.getPacketId());
        if (id == null) {
            var packet = recomhService.findLastPacket();
            id = ItemCriteria.renewalCommunicationPacket(packet.getId());
        }

        return Arrays.asList(
                (file != null) ? file : id,
                ItemCriteria.poliza(f.getPolicy()),
                ItemCriteria.channelAssigned(f.getChannel()),
                ItemCriteria.state(f.getState()),
                ItemCriteria.validation(f.getValidation()),
                ItemCriteria.segmento(f.getSegment()),
                ItemCriteria.renewalCommunicationPacket(f.getPacketId()),
                ItemCriteria.fecharenovacionEnd(f.getRenovationDateEnd()),
                ItemCriteria.fecharenovacionStart(f.getRenovationDateStart())
        );
    }
}
