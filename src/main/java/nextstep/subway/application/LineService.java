package nextstep.subway.application;

import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.Station;
import nextstep.subway.dto.LineRequest;
import nextstep.subway.dto.LineResponse;
import nextstep.subway.dto.StationResponse;
import nextstep.subway.exception.NotFoundException;
import nextstep.subway.repository.LineRepository;
import nextstep.subway.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LineService {
    private final LineRepository lineRepository;

    private final StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Station upStation = stationRepository.findById(lineRequest.getUpStationId())
                .orElseThrow(() -> new NotFoundException("등록된 지하철역이 없습니다."));
        Station downStation = stationRepository.findById(lineRequest.getDownStationId())
                .orElseThrow(() -> new NotFoundException("등록된 지하철역이 없습니다."));
        Line line = lineRequest.toLine();
        line.addUpStation(upStation);
        line.addDownStation(downStation);
        Line persistStation = lineRepository.save(line);
        return LineResponse.of(persistStation);
    }

    @Transactional
    public LineResponse updateLine(long id, LineRequest lineRequest) {
        LineResponse line = findLineById(id);
        List<StationResponse> stations = line.getStations();
        Station upStation = stationRepository.findById(stations.get(0).getId())
                .orElseThrow(() -> new NotFoundException("등록된 지하철역이 없습니다."));
        Station downStation = stationRepository.findById(stations.get(1).getId())
                .orElseThrow(() -> new NotFoundException("등록된 지하철역이 없습니다."));
        Line newLine = Line.builder(lineRequest.getName(), lineRequest.getColor(), line.getDistance())
                .id(line.getId())
                .build()
                .addUpStation(upStation)
                .addDownStation(downStation);
        Line persistStation = lineRepository.save(newLine);
        return LineResponse.of(persistStation);
    }

    public List<LineResponse> findAllLines() {
        return lineRepository.findAll().stream()
                .map(LineResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse findLineById(Long id) {
        return LineResponse.of(lineRepository.findById(id).orElseThrow(() -> new NotFoundException("등록된 노선이 없습니다.")));
    }

    @Transactional
    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }
}
