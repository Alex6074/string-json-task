package ru.clevertec.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.clevertec.domain.Customer;
import ru.clevertec.domain.Order;
import ru.clevertec.domain.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSerializerTest {

    private static ObjectMapper objectMapper;
    private final JsonSerializer jsonSerializer = new JsonSerializer();

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @ParameterizedTest
    @MethodSource("productProvider")
    void shouldReturnProductInJsonFormat(UUID id, String name, double price, Map<UUID, BigDecimal> map) throws JsonProcessingException {
        //given
        Product product = new Product(id, name, price, map);

        //when
        String myJson = jsonSerializer.toJson(product);
        String jacksonJson = objectMapper.writeValueAsString(product);

        //then
        assertEquals(jacksonJson, myJson);
    }

    @ParameterizedTest
    @MethodSource("orderProvider")
    void shouldReturnOrderInJsonFormat(UUID id, List<Product> products, OffsetDateTime createDate) throws JsonProcessingException {
        //given
        Order order = new Order(id, products, createDate);

        //when
        String myJson = jsonSerializer.toJson(order);
        String jacksonJson = objectMapper.writeValueAsString(order);

        //then
        assertEquals(jacksonJson, myJson);
    }

    @ParameterizedTest
    @MethodSource("customerProvider")
    void shouldReturnCustomerInJsonFormat(UUID id, String firstName, String lastName, LocalDate dateBirth, List<Order> orders) throws JsonProcessingException {
        //given
        Customer customer = new Customer(id, firstName, lastName, dateBirth, orders);

        //when
        String myJson = jsonSerializer.toJson(customer);
        String jacksonJson = objectMapper.writeValueAsString(customer);

        //then
        assertEquals(jacksonJson, myJson);
    }

    static Stream<Arguments> customerProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), "John", "Snow", LocalDate.now(),
                        List.of(new Order(UUID.randomUUID(), List.of(
                                new Product(UUID.randomUUID(), "Laptop", 1000.0, Map.of())),
                                OffsetDateTime.now()))),

                Arguments.of(UUID.randomUUID(), "Jane", "Smith",
                        LocalDate.of(1985, 3, 10), List.of()),

                Arguments.of(null, null, null, LocalDate.of(2000, 1, 1), null),

                Arguments.of(UUID.randomUUID(), "Michael", "Johnson", LocalDate.now().plusDays(5),
                        List.of(new Order(UUID.randomUUID(), List.of(
                                new Product(UUID.randomUUID(), "Apple", 500.0, Map.of())),
                                OffsetDateTime.now().minusDays(10)))),

                Arguments.of(UUID.randomUUID(), "Alice", "Williams", null, null)
        );
    }

    static Stream<Arguments> orderProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(),
                        List.of(new Product(UUID.randomUUID(), "Orange", 1000.0, Map.of())),
                        OffsetDateTime.now()),

                Arguments.of(UUID.randomUUID(), List.of(), OffsetDateTime.now()),

                Arguments.of(null,
                        List.of(new Product(UUID.randomUUID(), "Phone", 700.0, Map.of())),
                        OffsetDateTime.now().plusDays(10)),

                Arguments.of(UUID.randomUUID(), List.of(
                                new Product(UUID.randomUUID(), "Apple", 500.0, Map.of()),
                                new Product(UUID.randomUUID(), "Watermelon", 150.0, Map.of())),
                        OffsetDateTime.now().minusDays(5)),

                Arguments.of(null, null, null)
        );
    }

    static Stream<Arguments> productProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), "Apple", 8.5,
                        Map.of(UUID.randomUUID(), BigDecimal.valueOf(6L))),
                Arguments.of(UUID.randomUUID(), "", 0.0, Map.of()),
                Arguments.of(UUID.randomUUID(), "Orange", Double.MAX_VALUE,
                        Map.of(UUID.randomUUID(), BigDecimal.valueOf(Long.MAX_VALUE),
                                UUID.randomUUID(), BigDecimal.valueOf(Long.MAX_VALUE))),
                Arguments.of(UUID.randomUUID(), "Watermelon", -10.0, null),
                Arguments.of(null, null, 15.75,
                        Map.of(UUID.randomUUID(), BigDecimal.ZERO,
                                UUID.randomUUID(), BigDecimal.valueOf(0L)))
                );
    }
}