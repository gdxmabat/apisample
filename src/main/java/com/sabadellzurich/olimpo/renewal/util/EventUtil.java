package com.sabadellzurich.olimpo.renewal.util;

import com.sabadellzurich.olimpo.renewal.common.lib.model.CommunicationItemLog;
import com.sabadellzurich.olimpo.renewal.common.lib.model.CommunicationTransactionLog;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Data
public class EventUtil<T> {
	private static EventUtil<CommunicationItemLog> itemUtil;
	private static EventUtil<CommunicationTransactionLog> packetUtil;

	public static EventUtil<CommunicationItemLog> getItemEventUtil() {
		if (itemUtil == null) {
			itemUtil = new EventUtil<>();
			itemUtil.setDefaultValue(CommunicationItemLog::getAction);

			itemUtil.add((i -> i.getAction().equals("PROCESS_TECHNICAl_ERROR")), "Error técnico en el proceso.");
			itemUtil.add((i -> i.getAction().equals("NO_PROCESS")), "No procesado.");

			itemUtil.add((i -> i.getAction().equals("ROBISON_CHECKED") && i.getResult().equals("OK_CHECKER")), "Validación Robinson correcta.");
			itemUtil.add((i -> i.getAction().equals("ROBISON_CHECKED") && !i.getResult().equals("OK_CHECKER")), "Validación Robinson incorrecta.");

			itemUtil.add((i -> i.getAction().equals("PROCESS_RENEWAL_STARTED")), "Proceso de renovación iniciado.");

			itemUtil.add((i -> i.getAction().equals("POSTAL_CHECKED") && i.getResult().equals("OK_CHECKER")), "Validación Postal correcta.");
			itemUtil.add((i -> i.getAction().equals("POSTAL_CHECKED") && !i.getResult().equals("OK_CHECKER")), "Validación Postal incorrecta.");

			itemUtil.add((i -> i.getAction().equals("SMS_CHECKED") && i.getResult().equals("OK_CHECKER")), "Validación de SMS correcta.");
			itemUtil.add((i -> i.getAction().equals("SMS_CHECKED") && !i.getResult().equals("OK_CHECKER")), "Validación de SMS incorrecta.");

			itemUtil.add((i -> i.getAction().equals("MAIL_CHECKED") && i.getResult().equals("OK_CHECKER")), "Validación de email correcta.");
			itemUtil.add((i -> i.getAction().equals("MAIL_CHECKED") && i.getResult().equals("NO_TESTED")), "Validación de email incorrecta, no se ha especificado el email.");
			itemUtil.add((i -> i.getAction().equals("MAIL_CHECKED") &&
					!i.getResult().equals("OK_CHECKER") &&
					!i.getResult().equals("NO_TESTED")), "Validación de email incorrecta.");

			itemUtil.add((i -> i.getAction().equals("CHANNEL_ASSIGNED") && (
					i.getResult().equals("POSTAL_ASSIGNED")) ||
					(i.getDescription() != null && i.getDescription().equals("POSTAL_ASSIGNED"))
			), "Canal asignado a envío postal.");
			itemUtil.add((i -> i.getAction().equals("CHANNEL_ASSIGNED") && i.getDescription() != null && i.getDescription().equals("SMS_ASSIGNED")), "Canal asignado a envío por SMS.");
			itemUtil.add((i -> i.getAction().equals("CHANNEL_ASSIGNED") && i.getDescription() != null && i.getDescription().equals("MAIL_ASSIGNED")), "Canal asignado a envío email.");

			itemUtil.add((i -> i.getAction().equals("MAILTECK_REQUEST")), "Petición enviada a MailTeck.");
			itemUtil.add((i -> i.getAction().equals("WHITECO_REQUEST")), "Petición enviada a White Co.");
			itemUtil.add((i -> i.getAction().equals("ONE_TO_ONE_REQUEST")), "Petición enviada a One to One.");
			itemUtil.add((i -> i.getAction().equals("WHITECO_RESPONSE")), "Recibida respuesta de White Co.");
			itemUtil.add((i -> i.getAction().equals("SAMPLE")), "Muestra.");
			itemUtil.add((i -> i.getAction().equals("COMMUNICATION_REQUEST")), "Petición de comunicaciones enviada.");
			itemUtil.add((i -> i.getAction().equals("VALIDATION_OK")), "Validación correcta.");
			itemUtil.add((i -> i.getAction().equals("VALIDACION_NOK")), "Validación incorrecta.");
			itemUtil.add((i -> i.getAction().equals("BUSINESS_VALIDATION_OK")), "Validación de negocio correcta.");
			itemUtil.add((i -> i.getAction().equals("BUSINESS_VALIDATION_NOK")), "Validación de negocio incorrecta.");
			itemUtil.add((i -> i.getAction().equals("COMMUNICATION_SENT")), "Comunicación enviada.");
			itemUtil.add((i -> i.getAction().equals("SAMPLE_SEND_OK")), "Muestra enviada.");
		}

		return itemUtil;
	}

	public static EventUtil<CommunicationTransactionLog> getPacketEventUtil() {
		if (packetUtil == null) {
			packetUtil = new EventUtil<>();
			packetUtil.setDefaultValue(CommunicationTransactionLog::getNewStatus);

			packetUtil.add((i -> i.getNewStatus().equals("CREATED")), "Procesamiento iniciado.");
			packetUtil.add((i -> i.getNewStatus().equals("CHANNEL_ASSIGNATION_DIRECT_MAIL_STARTED")), "Iniciando asignación de canal a envíos postal.");
			packetUtil.add((i -> i.getNewStatus().equals("CHANNEL_ASSIGNATION_DIRECT_MAIL_FINISHED")), "Canales asignados a envíos postal.");
			packetUtil.add((i -> i.getNewStatus().equals("CHANNEL_ASSIGNATION_FINISHED")), "Canales asignados a envíos digitales.");
			packetUtil.add((i -> i.getNewStatus().equals("PROVIDERS_DELIVERY_WAITING")), "Envíado a proveedores.");
			packetUtil.add((i -> i.getNewStatus().equals("LINKS_RECEIVED")), "Respuesta recibida de proveedores.");
			packetUtil.add((i -> i.getNewStatus().equals("SAMPLES_GENERATION_FINISHED")), "Muestras generadas.");
			packetUtil.add((i -> i.getNewStatus().equals("COMMUNICATIONS_SENT")), "Comunicaciones enviadas.");
			packetUtil.add((i -> i.getNewStatus().equals("PROCESS_TECHNICAl_ERROR")), "Error técnico en el proceso.");
			packetUtil.add((i -> i.getNewStatus().equals("NO_PROCESS")), "No procesado.");
			packetUtil.add((i -> i.getNewStatus().equals("CHECKER_EMAIL_STARTED")), "Validación de email iniciada.");
			packetUtil.add((i -> i.getNewStatus().equals("CHECKER_EMAIL_FINISHED")), "Validación de email finalizada.");
			packetUtil.add((i -> i.getNewStatus().equals("CHECKER_SMS_STARTED")), "Validación de SMS iniciada.");
			packetUtil.add((i -> i.getNewStatus().equals("CHECKER_SMS_WAITING")), "Validación de SMS pendiente.");
			packetUtil.add((i -> i.getNewStatus().equals("CHECKER_SMS_FINISHED")), "Validación de SMS finalizada.");
			packetUtil.add((i -> i.getNewStatus().equals("CHANNEL_ASSIGNATION_FINISHED_STARTED")), "Asignación final (Postal).");
			packetUtil.add((i -> i.getNewStatus().equals("SEEDS_GENERATED")), "Semillas generadas.");
			packetUtil.add((i -> i.getNewStatus().equals("PROVIDERS_DELIVERY_STARTED")), "Envío a proveedores iniciado.");
			packetUtil.add((i -> i.getNewStatus().equals("PROVIDERS_DELIVERY_FINISHED")), "Envío a proveedores finalizado.");
			packetUtil.add((i -> i.getNewStatus().equals("SAMPLES_GENERATION_STARTED")), "Generación de muestras iniciado.");
			packetUtil.add((i -> i.getNewStatus().equals("SAMPLES_GENERATION_FINISHED")), "Generación de muestras finalizado.");
			packetUtil.add((i -> i.getNewStatus().equals("DELIVERY_VALIDATION_STARTED")), "Validación de entrega iniciada.");
			packetUtil.add((i -> i.getNewStatus().equals("DELIVERY_VALIDATION_FINISHED")), "Validación de entrega finalizada.");
			packetUtil.add((i -> i.getNewStatus().equals("DELIVERY_VALIDATION_WAITING")), "Validación de entrega pendiente.");
			packetUtil.add((i -> i.getNewStatus().equals("VALIDATION_RECIEVED_OK")), "Validación de entrega correcta.");
			packetUtil.add((i -> i.getNewStatus().equals("VALIDATION_RECIEVED_NOK")), "Validación de entrega incorrecta.");
			packetUtil.add((i -> i.getNewStatus().equals("DIGITAL_DELIVERY_STARTED")), "Envío digital iniciado.");
			packetUtil.add((i -> i.getNewStatus().equals("DIGITAL_DELIVERY_END")), "Envío digital finalizado.");
			packetUtil.add((i -> i.getNewStatus().equals("VALIDATION_STATUS_OK")), "Validación correcta.");
			packetUtil.add((i -> i.getNewStatus().equals("VALIDATION_STATUS_NOK")), "Validación incorrecta.");
			packetUtil.add((i -> i.getNewStatus().equals("BUSINESS_VALIDATION_OK")), "Validación de negocio correcta.");
			packetUtil.add((i -> i.getNewStatus().equals("BUSINESS_VALIDATION_NOK")), "Validación de negocio incorrecta.");
			packetUtil.add((i -> i.getNewStatus().equals("COMMUNICATION_SENT")), "Comunicación enviada.");
			packetUtil.add((i -> i.getNewStatus().equals("CLOSED")), "Cerrado.");
		}

		return packetUtil;
	}

	private Map<Function<T, Boolean>, String> eventFilters = new HashMap<>();

	private Function<T, String> defaultValue;

	public void add(Function<T, Boolean> key, String value) {
		eventFilters.put(key, value);
	}

	public String parseEvent(T log) {
		for (var filter : eventFilters.entrySet()) {
			if (filter.getKey().apply(log)) {
				return filter.getValue();
			}
		}
		if (defaultValue != null) {
			return defaultValue.apply(log);
		}

		return "Undefined";
	}
}
