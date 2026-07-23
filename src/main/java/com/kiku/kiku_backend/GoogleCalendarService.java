package com.kiku.kiku_backend;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleCalendarService {

    @Value("${google.client.email}")
    private String clientEmail;

    @Value("${google.private.key}")
    private String privateKey;

    @Value("${google.calendar.id}")
    private String calendarId;

    private Calendar getCalendarService() throws Exception {
        String keyFormatted = privateKey.replace("\\n", "\n");

        String json = String.format("""
            {
              "type": "service_account",
              "project_id": "kiku-493110",
              "private_key_id": "f1d89a939530b76902407e4e0d551ce2d493ef45",
              "private_key": "%s",
              "client_email": "%s",
              "client_id": "100905915736058160646",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token"
            }
            """, keyFormatted.replace("\"", "\\\""), clientEmail);

        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                .createScoped(List.of("https://www.googleapis.com/auth/calendar"));

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Kiku")
                .build();
    }

    public String createMeetingEvent(String guestEmail, String guestName,
                                      LocalDate date, LocalTime time,
                                      String sessionType) throws Exception {
        Calendar service = getCalendarService();

        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        ZonedDateTime start = ZonedDateTime.of(date, time, tokyoZone);
        ZonedDateTime end = start.plusMinutes(50);

        Event event = new Event()
                .setSummary("Kiku Session — " + guestName)
                .setDescription("Session type: " + sessionType + " | Guest: " + guestEmail);

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(start.toInstant().toEpochMilli()))
                .setTimeZone("Asia/Tokyo"));

        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(end.toInstant().toEpochMilli()))
                .setTimeZone("Asia/Tokyo"));

        event.setConferenceData(new ConferenceData()
                .setCreateRequest(new CreateConferenceRequest()
                        .setRequestId(UUID.randomUUID().toString())
                        .setConferenceSolutionKey(new ConferenceSolutionKey().setType("eventHangout"))));

        Event created = service.events().insert(calendarId, event)
                .setConferenceDataVersion(1)
                .setSendUpdates("none")
                .execute();

        return created.getHangoutLink();
    }
}