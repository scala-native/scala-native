```
docker run -i -t scalanative/scalanative-drone:0.2.4
```


# Publish docker image

```
docker build -t scalanative/scalanative-drone:0.2.4 .
docker login
docker push scalanative/scalanative-drone:0.2.4
```