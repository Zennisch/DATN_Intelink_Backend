package intelink.services.interfaces;

import java.util.List;

import intelink.models.ShortUrlAnalysisResult;

public interface IAnalysisResultService {

    ShortUrlAnalysisResult save(ShortUrlAnalysisResult result);

    List<ShortUrlAnalysisResult> saveAll(List<ShortUrlAnalysisResult> results);

}
