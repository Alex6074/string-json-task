package ru.clevertec.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.clevertec.domain.Customer;
import ru.clevertec.domain.Order;
import ru.clevertec.domain.Product;
import ru.clevertec.exception.JsonParseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonDeserializerTest {

    private static ObjectMapper objectMapper;
    private final JsonDeserializer jsonDeserializer = new JsonDeserializer();

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @ParameterizedTest
    @MethodSource("productProvider")
    void shouldReturnProduct(UUID id, String name, double price, Map<UUID, BigDecimal> map) throws JsonProcessingException {
        //given
        Product product = new Product(id, name, price, map);
        String jacksonJson = objectMapper.writeValueAsString(product);

        //when
        Product deserializedProduct = jsonDeserializer.fromJson(jacksonJson, Product.class);

        //then
        assertEquals(product, deserializedProduct);
        assertNotSame(product, deserializedProduct);
    }

    @Test
    void shouldThrowJsonParseException_whenInvalidJsonProduct() {
        //given
        String invalidJson = "{\"id\":123,\"name\":\"Apple\",\"price\":\"invalid\",\"map\":{}}";

        //when, then
        assertThrows(JsonParseException.class,
                () -> jsonDeserializer.fromJson(invalidJson, Product.class));
    }

    @Test
    void shouldThrowJsonParseException_whenInvalidJsonOrder() {
        //given
        String invalidJson = "{\"id\":\"invalid_uuid\",\"products\":[],\"createDate\":\"2023-09-21T10:10\"}";

        //when, then
        assertThrows(JsonParseException.class,
                () -> jsonDeserializer.fromJson(invalidJson, Order.class));
    }

    @Test
    void shouldThrowJsonParseException_whenInvalidJsonCustomer() {
        //given
        String invalidJson = "{\"id\":null,\"firstName\":123,\"lastName\":\"Doe\",\"dateBirth\":\"invalid_date\"}";

        //when, then
        assertThrows(JsonParseException.class,
                () -> jsonDeserializer.fromJson(invalidJson, Customer.class));
    }

    @ParameterizedTest
    @MethodSource("orderProvider")
    void shouldReturnOrderInJsonFormat(UUID id, List<Product> products, OffsetDateTime createDate) throws JsonProcessingException {
        //given
        Order order = new Order(id, products, createDate);
        String jacksonJson = objectMapper.writeValueAsString(order);

        //when
        Order deserializedOrder = jsonDeserializer.fromJson(jacksonJson, Order.class);

        //then
        assertEquals(order, deserializedOrder);
        assertNotSame(order, deserializedOrder);
    }

    @ParameterizedTest
    @MethodSource("customerProvider")
    void shouldReturnCustomerInJsonFormat(UUID id, String firstName, String lastName, LocalDate dateBirth, List<Order> orders) throws JsonProcessingException {
        //given
        Customer customer = new Customer(id, firstName, lastName, dateBirth, orders);
        String jacksonJson = objectMapper.writeValueAsString(customer);

        //when
        Customer deserializedCustomer = jsonDeserializer.fromJson(jacksonJson, Customer.class);

        //then
        assertEquals(customer, deserializedCustomer);
        assertNotSame(customer, deserializedCustomer);
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