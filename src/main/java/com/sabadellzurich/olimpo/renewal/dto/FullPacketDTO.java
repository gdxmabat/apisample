package com.sabadellzurich.olimpo.renewal.dto;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.PacketDTO;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.Timeline;
import com.sabadellzurich.olimpo.renewal.common.lib.enumstatus.ChannelAssignedEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Full packet DTO.
 * ================
 * <p>
 * Packet DTO that will be printed in the packet detail screen.
 *
 * @author msellers
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullPacketDTO extends PacketDTO {
    private Map<ChannelAssignedEnum, Stats> stats = new HashMap<>();
    private List<Timeline> events = new LinkedList<>();

    /**
     * Exports the packet to a CSV string.
     * <p>
     * TODO Find a way to do it with jackson easily.
     *
     * @return Packet's csv representation.
     */
    public String export() throws JsonProcessingException {
        // Map stats
        var statsHeader = "type|total|sent|notSent|error|landing";
        var statsStr = new StringJoiner(";");
        statsStr.add(statsHeader);
        stats.forEach((k, v) -> {
            var statStr = new StringJoiner("|");
            statStr.add(k.toString())
                    .add(v.getTotal() + "")
                    .add(v.getSent() + "")
                    .add(v.getNotSent() + "")
                    .add(v.getError() + "")
                    .add(v.getLanding() + "");

            statsStr.add(statStr.toString());
        });

        // Map events
        var eventsHeader = "date|text";
        var eventsStr = new StringJoiner(";");
        eventsStr.add(eventsHeader);
        events.forEach(e -> {
            var eventStr = new StringJoiner("|");
            eventStr.add(e.getDate().format(DateTimeFormatter.ISO_DATE_TIME))
                    .add(e.getText());

            eventsStr.add(eventStr.toString());
        });

        var mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        var schema = mapper.schemaFor(CsvFullPacketDTO.class)
                .withColumnSeparator(',')
                .withHeader();

        var packet = new ModelMapper().map(this, CsvFullPacketDTO.class);
        packet.setStats(statsStr.toString());
        packet.setEvents(eventsStr.toString());

        return mapper.writer(schema)
                .writeValueAsString(packet);
    }

    /**
     * Packet statistics.
     * ==================
     * <p>
     * Represents the communication statistics of a packet.
     *
     * @author msellers.
     */
    @Data
    public static class Stats {
        private long total;
        private long sent;
        private long notSent;
        private long error;
        private long landing;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class CsvFullPacketDTO extends PacketDTO {
        private String stats;
        private String events;
    }
}
