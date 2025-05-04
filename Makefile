# Makefile

VERSION=1.0.1

.PHONY: build-be push-be build-fe push-fe all

build-be:
	docker build -t quocanhh86/fpl-student-attendance-be:$(VERSION) -f backend/Dockerfile backend

push-be: build-be
	docker push quocanhh86/fpl-student-attendance-be:$(VERSION)

build-fe:
	docker build -t quocanhh86/fpl-student-attendance-fe:$(VERSION) -f frontend/Dockerfile frontend

push-fe: build-fe
	docker push quocanhh86/fpl-student-attendance-fe:$(VERSION)

all: push-be push-fe
