package com.example.oliveyoung.controller;

import com.example.oliveyoung.service.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/admin")
public class GitController {

    private final GitService gitService;

    public GitController(GitService gitService) {
        this.gitService = gitService;
    }

    @PostMapping("/pod/scale")
    public CompletableFuture<ResponseEntity<String>> podScaleService(@RequestParam int replicas) throws GitAPIException, IOException {
        return gitService.updatePodMinReplicas(replicas)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + ex.getMessage()));
    }

    @PostMapping("/node/scale")
    public CompletableFuture<ResponseEntity<String>> nodScaleService(@RequestParam int replicas) throws GitAPIException, IOException {
        return gitService.updateNodeMinReplicas(replicas)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + ex.getMessage()));
    }

    @PostMapping("/schedule/pod")
    public CompletableFuture<ResponseEntity<String>> schedulePodScaling(@RequestBody ScheduleRequest request) {
        return gitService.schedulePodScaling(request.getReplicas(), request.getDelayInMillis())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    @PostMapping("/schedule/node")
    public CompletableFuture<ResponseEntity<String>> scheduleNodeScaling(@RequestBody ScheduleRequest request) {
        return gitService.scheduleNodeScaling(request.getReplicas(), request.getDelayInMillis())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }
}

// 새로운 DTO 클래스 추가
class ScheduleRequest {
    private int replicas;
    private long delayInMillis;

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public long getDelayInMillis() {
        return delayInMillis;
    }

    public void setDelayInMillis(long delayInMillis) {
        this.delayInMillis = delayInMillis;
    }
}