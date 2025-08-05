package intelink.services.interfaces;

import intelink.models.AnalysisResult;

import java.util.List;

public interface IAnalysisResultService {

    AnalysisResult save(AnalysisResult result);

    List<AnalysisResult> saveAll(List<AnalysisResult> results);

}
