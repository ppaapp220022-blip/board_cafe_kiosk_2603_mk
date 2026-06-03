package org.example.board_cafe_kiosk_2603.global.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job dailyRevenueJob;

    @InjectMocks
    private StatScheduler statScheduler;

    @Test
    void runManualStatJob_usesTargetDateAndUniqueTime() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 5, 1);

        statScheduler.runManualStatJob(targetDate);

        ArgumentCaptor<JobParameters> jobParametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(org.mockito.Mockito.eq(dailyRevenueJob), jobParametersCaptor.capture());

        JobParameters captured = jobParametersCaptor.getValue();
        assertThat(captured.getString("targetDate")).isEqualTo("2026-05-01");
        assertThat(captured.getLong("time")).isNotNull();
    }
}
