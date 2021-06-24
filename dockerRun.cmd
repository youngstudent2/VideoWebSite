docker stop video-website-1-1 video-website-1-2 video-website-1-3 video-website-1-4
docker rm video-website-1-1 video-website-1-2 video-website-1-3 video-website-1-4
docker run -d --name video-website-1-1 --cpus=2 -p 8081:8090 video-website
docker run -d --name video-website-1-2 --cpus=2 -p 8082:8090 video-website
docker run -d --name video-website-1-3 --cpus=2 -p 8083:8090 video-website
docker run -d --name video-website-1-4 --cpus=2 -p 8084:8090 video-website
cd encode_end
start mvn spring-boot:run
cd ../
