# WebFlux Orders API

This project is a reactive RESTful API for managing orders. It is written in Java and uses reactive programming with Spring WebFlux and MongoDB.

**Note:** This API is part of a larger e-commerce system that includes product catalog management and user management. To run the complete system, you must also run the [WebFlux Catalog API](https://github.com/anabeatrizdmt/webflux-catalog), the [WebFlux Users API](https://github.com/anabeatrizdmt/webflux-users), and the [WebFlux E-commerce API Gateway](https://github.com/anabeatrizdmt/webflux-ecommerce-api-gateway). The API Gateway provides a unified interface to the entire system and manages communication between the individual APIs.

## Requirements

- Java 11 or higher
- MongoDB

## Installation and Setup

1. Clone the repository:

```
git clone https://github.com/anabeatrizdmt/webflux-orders.git
```

2. Install dependencies:

```
cd webflux-orders
mvn install
```

3. Run the application:

```
mvn spring-boot:run
```

## Usage

### Create an order

To create a new order, send a `POST` request to `http://localhost:8080/orders` with a JSON payload in the following format:


```
{
"productList": [
  {
    "product": "product-id-1",
    "quantity": 2
  },
  {
    "product": "product-id-2",
    "quantity": 2
  }
],
"userId": "user-id"
}
```

### Get all orders

To retrieve a list of all orders, send a `GET` request to `http://localhost:8080/orders`.

### Get an order by ID

To retrieve a specific order by its ID, send a `GET` request to `http://localhost:8080/orders/{id}`, where `{id}` is the ID of the order you wish to retrieve.



## Order Status

The order status is automatically updated based on the following conditions:

- If there isn't enough stock for any product in the order, the order will not be created.
- If the order is created it gets the status `PLACED`.
- The user status is checked:
-- If the user's status is `ACTIVE`, the status of the order will be updated to `CONFIRMED`.
-- If the user's status is `BLOCKED`, the status of the order will be updated to `ERROR_IN_ORDER`.
- If the order is confirmed, a request is made the catalog to update the stock and the status of the order will be updated to `SENT_FOR_DELIVERY`.



## Dependencies

This repository depends on the following endpoints:

### User Status

To check the status of a user, it sends a `GET` request to `http://localhost:8080/users/status/{id}`. See more in: [WebFlux Users API](https://github.com/anabeatrizdmt/webflux-users)

### Product Stock

To check the stock of one or more products, it sends a `POST` request to `http://localhost:8081/catalog/stock`. See more in: [WebFlux Catalog API](https://github.com/anabeatrizdmt/webflux-catalog)

### Update Product Stock

To update the stock of one or more products, it sends a `POST` request to `http://localhost:8081/catalog/update-stock`. See more in: [WebFlux Catalog API](https://github.com/anabeatrizdmt/webflux-catalog)

