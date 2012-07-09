package ciknow.recommend.rescore;

import ciknow.domain.Recommendation;

public interface Rescorer {
	public void rescore(Recommendation rec);
}
