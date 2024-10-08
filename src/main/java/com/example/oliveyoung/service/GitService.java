package com.example.oliveyoung.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
public class GitService {

    private static final String REMOTE_URL = "https://github.com/JinsuYeo/hachwimu-iac.git";
    private static final String LOCAL_REPO_PATH = "/app/repo/iac-repo";  // 로컬 저장소 경로
    private static final String BRANCH_NAME = "main";  // 브랜치 이름

    private final CredentialsProvider credentialsProvider;
    private final DynamicSchedulerService dynamicSchedulerService;

    public GitService(CredentialsProvider credentialsProvider, DynamicSchedulerService dynamicSchedulerService) {
        this.credentialsProvider = credentialsProvider;
        this.dynamicSchedulerService = dynamicSchedulerService;
    }

    @Async
    public CompletableFuture<String> scheduleNodeScaling(int replicas, long delayInMillis) {
        dynamicSchedulerService.scheduleTask("nodeScaling", () -> {
            try {
                // Node 스케일링 로직 호출
                updateNodeMinReplicas(replicas);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delayInMillis);

        return CompletableFuture.completedFuture("Node 스케일링이 예약되었습니다.");
    }


    @Async
    public CompletableFuture<String> schedulePodScaling(int replicas, long delayInMillis) {
        dynamicSchedulerService.scheduleTask("podScaling", () -> {
            try {
                // Pod 스케일링 로직 호출
                updatePodMinReplicas(replicas);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delayInMillis);

        return CompletableFuture.completedFuture("Pod 스케일링이 예약되었습니다.");
    }


    @Async // 비동기적으로 작업을 처리
    public CompletableFuture<String> updatePodMinReplicas(int replicas) throws GitAPIException, IOException {
        // 1. 로컬 저장소 Clone or Pull
        Git git = pullOrCloneRepository();

        // 2. hpa.yaml 파일 경로
        Path yamlFilePath = Paths.get(LOCAL_REPO_PATH, "hpa.yaml");

        // 3. 파일 수정
        modifyPodYamlFile(yamlFilePath, replicas);

        // 4. Git Add, Commit, Push
        git.add().addFilepattern("hpa.yaml").call();
        git.commit().setMessage("Updated minReplicas for service-products to " + replicas)
                .setAuthor("jaebinGit", "tnwoql327@gmail.com")
                .setCommitter("jaebinGit", "tnwoql327@gmail.com")
                .call();
        git.push().setCredentialsProvider(credentialsProvider).call();

        return CompletableFuture.completedFuture("Successfully updated minReplicas for service-products to " + replicas + " and pushed to repository.");
    }

    @Async // 비동기적으로 작업을 처리
    public CompletableFuture<String> updateNodeMinReplicas(int replicas) throws GitAPIException, IOException {
        // 1. 로컬 저장소 Clone or Pull
        Git git = pullOrCloneRepository();

        // 2. overprovision.yaml 파일 경로
        Path yamlFilePath = Paths.get(LOCAL_REPO_PATH, "overprovision.yaml");

        // 3. 파일 수정
        modifyNodeYamlFile(yamlFilePath, replicas);

        // 4. Git Add, Commit, Push
        git.add().addFilepattern("overprovision.yaml").call();
        git.commit().setMessage("Updated replicas for node to " + replicas)
                .setAuthor("jaebinGit", "tnwoql327@gmail.com")  // 작성자 설정
                .setCommitter("jaebinGit", "tnwoql327@gmail.com") // 커미터 설정
                .call();
        git.push().setCredentialsProvider(credentialsProvider).call();

        return CompletableFuture.completedFuture("Successfully updated replicas for nginx to " + replicas + " and pushed to repository.");
    }


    private Git pullOrCloneRepository() throws GitAPIException, IOException {
        File repoDir = new File(LOCAL_REPO_PATH);
        Git git;

        if (repoDir.exists()) {
            // 이미 로컬에 저장소가 존재하는 경우 pull
            git = Git.open(repoDir);
            git.pull().setCredentialsProvider(credentialsProvider).call();
        } else {
            // 로컬에 저장소가 없으면 clone
            git = Git.cloneRepository()
                    .setURI(REMOTE_URL)
                    .setDirectory(repoDir)
                    .setBranch(BRANCH_NAME)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
        }

        return git;
    }

    private void modifyPodYamlFile(Path filePath, int replicas) throws IOException {
        // 파일을 읽어온 후 minReplicas 값을 찾아서 교체
        String content = new String(Files.readAllBytes(filePath));
        String modifiedContent = modifyMinReplicasInHpa(content, replicas);  // 서비스 프로덕트만 변경
        Files.write(filePath, modifiedContent.getBytes());
    }

    private void modifyNodeYamlFile(Path filePath, int replicas) throws IOException {
        // 파일을 읽어온 후 replicas 값을 교체
        String content = new String(Files.readAllBytes(filePath));
        String modifiedContent = modifyReplicasInOverprovision(content, replicas);  // nginx 서비스의 replicas 변경
        Files.write(filePath, modifiedContent.getBytes());
    }


    // service-products HorizontalPodAutoscaler만을 대상으로 minReplicas 값을 업데이트합니다.
    public static String modifyMinReplicasInHpa(String content, int replicas) {
        // service-products HorizontalPodAutoscaler만을 대상으로 minReplicas 값을 업데이트
        String regex = "(?s)(metadata:\\s*name:\\s*service-hpa-products.*?minReplicas:\\s*)\\d+";
        return content.replaceAll(regex, "$1" + replicas);
    }

    // overprovision.yaml에서 replicas 값을 업데이트합니다.
    public static String modifyReplicasInOverprovision(String content, int replicas) {
        // nginx Deployment에서 replicas 값을 업데이트
        String regex = "(?m)(replicas:\\s*)\\d+";
        return content.replaceAll(regex, "$1" + replicas);
    }
}