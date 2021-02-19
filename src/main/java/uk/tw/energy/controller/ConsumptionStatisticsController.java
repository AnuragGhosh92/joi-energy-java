package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.tw.energy.service.AccountService;

@RestController
@RequestMapping("/stats")
public class ConsumptionStatisticsController
{
	private AccountService accountService;

	public ConsumptionStatisticsController(final AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/lastWeek/{smartMeterId}")
	public ResponseEntity getLastWeekUsage(@PathVariable String smartMeterId) {
		Optional<BigDecimal> usage = accountService.getLastWeekUsage(smartMeterId);
		return usage.isPresent() ? ResponseEntity.ok(usage.get()) : ResponseEntity.notFound().build();
	}
}
