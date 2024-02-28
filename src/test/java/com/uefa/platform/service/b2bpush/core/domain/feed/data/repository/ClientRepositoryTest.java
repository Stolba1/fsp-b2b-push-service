package com.uefa.platform.service.b2bpush.core.domain.feed.data.repository;

import com.uefa.platform.service.b2bpush.AbstractIntegrationTest;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Client;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.EventPackage;
import com.uefa.platform.service.b2bpush.core.domain.feed.data.entity.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ClientRepositoryTest extends AbstractIntegrationTest {

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    @Autowired
    private ClientRepository repo;

    @BeforeEach
    public void setup() {
        repo.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        repo.deleteAll();
    }

    @Test
    public void testUpdateLastSentTime() {
        final Instant insertionTime = Instant.now().minus(1, ChronoUnit.MINUTES);
        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "feedId1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId2", List.of(parameter1),
                "hash2", insertionTime, Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY1", List.of(configuration1, configuration2), Status.ACTIVE, Instant.now(), EventPackage.BASIC);
        Client client2 =
                new Client("id2", "Broadcaster", "TEST_KEY2", List.of(configuration1, configuration2), Status.ACTIVE, Instant.now(), EventPackage.BASIC);

        repo.save(client);
        repo.save(client2);

        repo.updateLastSentDate(client.getId(), configuration2.getId());

        final Optional<Client> result = repo.findById(client.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertNotNull(result.get().getConfigurations().get(0));
        Assertions.assertEquals(configuration1.getId(), result.get().getConfigurations().get(0).getId());
        Assertions.assertEquals(formatter.format(configuration1.getLastSentTime()),
                formatter.format(result.get().getConfigurations().get(0).getLastSentTime()));

        Assertions.assertEquals(configuration1.getHash(), result.get().getConfigurations().get(0).getHash());

        Assertions.assertEquals(configuration2.getId(), result.get().getConfigurations().get(1).getId());
        Assertions.assertNotEquals(formatter.format(configuration2.getLastSentTime()),
                formatter.format(result.get().getConfigurations().get(1).getLastSentTime()));
        Assertions.assertEquals(configuration2.getHash(), result.get().getConfigurations().get(1).getHash());
    }

    @Test
    public void testUpdateHash() {
        Client.FeedConfiguration.Parameter parameter1 = new Client.FeedConfiguration.Parameter("competitionId", "1", null, null);
        Client.FeedConfiguration configuration1 = new Client.FeedConfiguration("configId1", "feedId1", List.of(parameter1),
                "hash1", Instant.now().minus(2, ChronoUnit.MINUTES), Status.ACTIVE, true);

        Client.FeedConfiguration configuration2 = new Client.FeedConfiguration("configId2", "feedId2", List.of(parameter1),
                "hash2", Instant.now(), Status.ACTIVE, true);

        Client client =
                new Client("id1", "Broadcaster", "TEST_KEY1", List.of(configuration1, configuration2), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);
        Client client2 =
                new Client("id2", "Broadcaster", "TEST_KEY2", List.of(configuration1, configuration2), Status.ACTIVE, Instant.now(), EventPackage.EXTENDED);

        repo.save(client);
        repo.save(client2);

        repo.updateHash(client.getId(), configuration2.getId(), "hash3");

        final Optional<Client> result = repo.findById(client.getId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertNotNull(result.get().getConfigurations().get(0));
        Assertions.assertEquals(configuration1.getId(), result.get().getConfigurations().get(0).getId());
        Assertions.assertEquals(configuration1.getHash(), result.get().getConfigurations().get(0).getHash());

        Assertions.assertEquals(configuration2.getId(), result.get().getConfigurations().get(1).getId());
        Assertions.assertEquals("hash3", result.get().getConfigurations().get(1).getHash());
    }


}

