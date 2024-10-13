package ru.clevertec;

import ru.clevertec.domain.Customer;
import ru.clevertec.domain.Order;
import ru.clevertec.domain.Product;
import ru.clevertec.helper.JsonDeserializer;
import ru.clevertec.helper.JsonSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class Main {


    public static void main(String[] args) {
        Product product1 = new Product(UUID.randomUUID(), "Apple", 5.5, Map.of(
                UUID.randomUUID(), BigDecimal.valueOf(6L)
        ));
        Product product2 = new Product(UUID.randomUUID(), "Orange", 10.1, Map.of(
                UUID.randomUUID(), BigDecimal.valueOf(Long.MAX_VALUE),
                UUID.randomUUID(), BigDecimal.valueOf(Long.MIN_VALUE)
        ));
        Order order1 = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.now());
        Order order2 = new Order(UUID.randomUUID(), List.of(product2), OffsetDateTime.now());
        Customer customer = new Customer(UUID.randomUUID(), "Ivan", "Ivanovich", LocalDate.now(), List.of(order1, order2));

        JsonSerializer jsonSerializer = new JsonSerializer();
        JsonDeserializer jsonDeserializer = new JsonDeserializer();

        String objectInJson = jsonSerializer.toJson(customer);
        Customer toCheck = jsonDeserializer.fromJson(objectInJson, Customer.class);

        System.out.println(objectInJson);
        System.out.println(toCheck.equals(customer));
    }
}