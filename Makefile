DOCKER_TAG  = sn-sphinx
DOCKER_FILE = Dockerfile.sphinx
DOCSDIR     = docs
BUILDDIR    = _build

.PHONY: setup_docs
setup_docs:
	docker build . -t $(DOCKER_TAG) -f $(DOCKER_FILE)

# Needs to run from the project root so that `sphinx-last-updated-by-git` can find the `.git` directory.
.PHONY: build_docs
build_docs: setup_docs
	docker run --rm -v .:/docs $(DOCKER_TAG) sphinx-build -M html $(DOCSDIR) $(DOCSDIR)/$(BUILDDIR) -W

