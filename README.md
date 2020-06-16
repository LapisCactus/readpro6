# ProPresenter6 slide file filter 

## About

In our church we are using ProPresentor6 from [RenewedVision](https://renewedvision.com/) and having hundreds of slide files.

We are going to change font size (and borders, colors, etc) of them to suit for live streaming for Sunday service.

This is not the first time to edit many slide files, but this slide file filter tool can achieve it with less effort.

## Build

```
mvnw clean package
```

## Run

```
java -jar target\readpro6.jar <filename>
```

## License

This tool , both in source format or binary format, can be freely distributed under the `Apache License 2.0`.
