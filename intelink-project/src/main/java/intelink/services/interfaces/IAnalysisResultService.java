package intelink.services.interfaces;

import intelink.models.ShortUrlAnalysisResult;

import java.util.List;

public interface IAnalysisResultService {

    ShortUrlAnalysisResult save(ShortUrlAnalysisResult result);

    List<ShortUrlAnalysisResult> saveAll(List<ShortUrlAnalysisResult> results);

}
