import { withWorkflow } from 'workflow/next';
import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  // API-only project — no image optimization or static pages needed
  images: { unoptimized: true },
};

export default withWorkflow(nextConfig);
