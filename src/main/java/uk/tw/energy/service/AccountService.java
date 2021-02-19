package uk.tw.energy.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

@Service
public class AccountService
{

	private final Map<String, String> smartMeterToPricePlanAccounts;

	private final MeterReadingService meterReadingService;

	private final List<PricePlan> pricePlans;

	public AccountService(Map<String, String> smartMeterToPricePlanAccounts,
			final MeterReadingService meterReadingService, final List<PricePlan> pricePlans) {
		this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
		this.meterReadingService = meterReadingService;
		this.pricePlans = pricePlans;
	}

	public String getPricePlanIdForSmartMeterId(String smartMeterId) {
		return smartMeterToPricePlanAccounts.get(smartMeterId);
	}

	public Optional<BigDecimal> getLastWeekUsage(String smartMeterId) {
		Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
		if (!readings.isPresent()) {
			return Optional.empty();
		}
		final String pricePlanId = getPricePlanIdForSmartMeterId(smartMeterId);
		List<ElectricityReading> filteredList = readings.get().stream().filter(this::isLastWeekReading)
				.sorted(Comparator.comparing(ElectricityReading::getTime)).collect(Collectors.toList());
		BigDecimal sum = filteredList.stream().map(reading -> reading.getReading()).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal averageReading = sum.divide(BigDecimal.valueOf(filteredList.size()));
		Long duration = Duration
				.between(filteredList.get(0).getTime(), filteredList.get(filteredList.size() - 1).getTime()).toHours();
		BigDecimal energyConsumed = averageReading.multiply(BigDecimal.valueOf(duration));

		PricePlan pricePlan = pricePlans.stream().filter(plan -> plan.getPlanName().equals(pricePlanId)).findFirst()
				.get();
		return Optional.of(energyConsumed.multiply(pricePlan.getUnitRate()));
	}

	private boolean isLastWeekReading(ElectricityReading reading) {
		Instant pastWeek = Instant.now().minus(Duration.ofDays(7));
		return reading.getTime().isAfter(pastWeek);
	}
}
