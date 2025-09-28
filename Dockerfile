FROM docker.klnsdr.com/nyx-cli:1.3 as builder

WORKDIR /app
COPY . .
RUN nyx build

FROM eclipse-temurin:21-jdk

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    git \
    podman \
    uidmap \
    slirp4netns \
    fuse-overlayfs \
    crun \
    iptables \
    sudo \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/build/ciclops-1.2.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
