# RabbitMQ

This folder contains several examples of working with RabbitMQ in java.
These examples demonstrate different client/libraries as well as advanced messaging topologies.

## Running Examples

1. Start a RabbitMQ broker. There is a bash file [drun.sh](./drun.sh), which can be used to start a single node RabbitMQ in docker. [Alternative installation options of RabbitMQ](https://www.rabbitmq.com/download.html) 
2. Examples declare objects in dedicated virtual hosts. [definitions.sh](./definitions.sh) can be used to create all required RabbitMQ objects.
3. jdk11
4. Each demo-* directory is a maven based project, which can be either imported into IDEA or run via `./mvnw spring-boot:run`. Please note that first example called "demo-aavanilla" is not a spring boot application.

For more information about each example check README.md file in corresponding directory.
