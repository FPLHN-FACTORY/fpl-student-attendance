VERSION=1.0.1

.PHONY: build-be push-be build-fe push-fe all

# Use Buildx to build for amd64
build-be:
	docker buildx build --platform linux/amd64 -t quocanhh86/fpl-student-attendance-be:$(VERSION) -f backend/Dockerfile backend --load

push-be: 
	docker buildx build --platform linux/amd64 -t quocanhh86/fpl-student-attendance-be:$(VERSION) -f backend/Dockerfile backend --push

build-fe:
	docker buildx build --platform linux/amd64 -t quocanhh86/fpl-student-attendance-fe:$(VERSION) -f frontend/Dockerfile frontend --load

push-fe: 
	docker buildx build --platform linux/amd64 -t quocanhh86/fpl-student-attendance-fe:$(VERSION) -f frontend/Dockerfile frontend --push

all: push-be push-fe
