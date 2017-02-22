# Run the raytracing demo in the docker image (932MB)

```
docker run -i -t scalacenter/scala-native:0.1.0
bin/nix-run
sbt
demoNative/run
```

# Publish docker image

```
docker build -t scalacenter/scala-native:0.1.0 .
docker login
docker push scalacenter/scala-native:0.1.0
```
